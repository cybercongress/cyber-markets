package fund.cyber.markets.ticker.service

import fund.cyber.markets.cassandra.model.CqlTokenTicker
import fund.cyber.markets.cassandra.repository.TickerRepository
import fund.cyber.markets.helpers.closestSmallerMultiplyFromTs
import fund.cyber.markets.kafka.JsonDeserializer
import fund.cyber.markets.kafka.JsonSerializer
import fund.cyber.markets.model.TokenTicker
import fund.cyber.markets.model.TokenTickerKey
import fund.cyber.markets.model.Trade
import fund.cyber.markets.ticker.configuration.TickersConfiguration
import io.reactivex.schedulers.Schedulers
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.TopicPartition
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.sql.Timestamp
import javax.annotation.PostConstruct

@Service
class TickerService {

    private val log = LoggerFactory.getLogger(TickerService::class.java)!!

    @Autowired lateinit var tickerRepository: TickerRepository
    @Autowired lateinit var configuration: TickersConfiguration

    //private var firstPoll: Boolean = true
    private var restoreNeeded: Boolean = false

    @PostConstruct
    fun test() {
        seekToEnd()
    }

    fun poll(): ConsumerRecords<String, Trade> {
/*        if (firstPoll) {
            seekToEnd()
            firstPoll = false
        }*/
        return consumer.poll(configuration.pollTimeout)
    }

    fun saveAndProduceToKafka(tickers: MutableMap<String, MutableMap<Long, TokenTicker>>) {
        val tickerSnapshots = mutableListOf<CqlTokenTicker>()
        val topicName = configuration.tickersTopicName

        //todo: correct timestamp?
        val currentMillisHop = closestSmallerMultiplyFromTs(configuration.windowHop)

        producer.beginTransaction()
        try {

            tickers.forEach { tokenSymbol, windowDurationMap ->
                windowDurationMap.forEach { windowDuration, ticker ->

                    val isClosedWindow = ticker.timestampTo <= currentMillisHop
                    val isSnapshot = ticker.timestampTo % windowDuration == 0L

                    if (isClosedWindow || configuration.allowNotClosedWindows) {
                        producer.send(produceRecord(CqlTokenTicker(ticker), topicName))
                    }
                    if (isClosedWindow && isSnapshot) {
                        tickerSnapshots.add(CqlTokenTicker(ticker))
                    }

                }
            }

        } catch (e: Exception) {
            log.error("Cannot produce ticker to kafka", e)
            producer.abortTransaction()
            Runtime.getRuntime().exit(-1)
        }
        producer.commitTransaction()

        if (tickerSnapshots.isNotEmpty()) {
            saveSnapshots(tickerSnapshots)
        }
    }

    private fun saveSnapshots(snapshots: MutableList<CqlTokenTicker>) {
        log.debug("Save tickers snapshots")

        Schedulers.io().scheduleDirect {
            try {
                tickerRepository.saveAll(snapshots).collectList().block()
            } catch (e: Exception) {
                backupTickerToKafka(snapshots)
            }
        }

        if (restoreNeeded) {
            restoreTickersFromKafka()
        }
    }

    private fun backupTickerToKafka(tickers: MutableList<CqlTokenTicker>) {
        log.info("Backuping tickers to kafka: {}", tickers.size)

        restoreNeeded = true
        producer.beginTransaction()
        try {
            tickers.forEach { ticker ->
                producer.send(produceRecord(ticker, configuration.tickersBackupTopicName))
            }
        } catch (e: Exception) {
            log.error("Tickers backup to kafka failed. Exiting app")
            producer.abortTransaction()
            Runtime.getRuntime().exit(-1)
        }
        producer.commitTransaction()
    }

    private fun restoreTickersFromKafka() {
        log.info("Restoring tickers from kafka")

        restoreNeeded = false
        Schedulers.single().scheduleDirect {
            val records = consumerBackup.poll(0)
            log.info("Tickers for restore count: {}", records.count())

            val tickers = records.map { it.value() }

            try {
                tickerRepository.saveAll(tickers).collectList().block()
            } catch (e: Exception) {
                restoreNeeded = true
                log.error("Tickers restore failed")
            }

        }
    }

    private fun produceRecord(ticker: CqlTokenTicker, topicName: String): ProducerRecord<TokenTickerKey, CqlTokenTicker> {
        return ProducerRecord(
                topicName,
                TokenTickerKey(ticker.symbol, ticker.interval, Timestamp(ticker.timestampTo)),
                ticker)
    }

    private fun seekToEnd() {
        consumer.poll(0)
        val partitions = mutableListOf<TopicPartition>()
        val tradeTopics = consumer.subscription()
        tradeTopics.forEach { topic ->
            consumer.partitionsFor(topic).forEach { partitionInfo ->
                partitions.add(TopicPartition(partitionInfo.topic(), partitionInfo.partition()))
            }
        }
        consumer.seekToEnd(partitions)
    }

    private val consumer by lazy { KafkaConsumer<String, Trade>(
            configuration.tickerConsumerConfig,
            JsonDeserializer(String::class.java),
            JsonDeserializer(Trade::class.java)
    ).apply {
        subscribe(configuration.topicNamePattern)
    } }

    private val consumerBackup by lazy { KafkaConsumer<TokenTickerKey, CqlTokenTicker>(
            configuration.tickersBackupConsumerConfig,
            JsonDeserializer(TokenTickerKey::class.java),
            JsonDeserializer(CqlTokenTicker::class.java)
    ).apply {
        subscribe(listOf(configuration.tickersBackupTopicName))
    }}

    private val producer by lazy { KafkaProducer<TokenTickerKey, CqlTokenTicker>(
            configuration.tickerProducerConfig,
            JsonSerializer<TokenTickerKey>(),
            JsonSerializer<CqlTokenTicker>()
    ).apply { initTransactions() }}

}