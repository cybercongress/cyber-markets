package fund.cyber.markets.ticker

import fund.cyber.markets.cassandra.model.CqlTicker
import fund.cyber.markets.cassandra.repository.TickerRepository
import fund.cyber.markets.kafka.JsonDeserializer
import fund.cyber.markets.kafka.JsonSerializer
import fund.cyber.markets.model.Ticker
import fund.cyber.markets.model.TickerKey
import fund.cyber.markets.model.TokensPair
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

@Service
class TickerService {

    private val log = LoggerFactory.getLogger(TickerService::class.java)!!

    @Autowired
    lateinit var configuration: TickersConfiguration

    @Autowired
    lateinit var tickerRepository: TickerRepository

    private val consumer by lazy { KafkaConsumer<String, Trade>(
            configuration.tickerConsumerConfig,
            JsonDeserializer(String::class.java),
            JsonDeserializer(Trade::class.java)
    ).apply {
        subscribe(configuration.topicNamePattern)
    } }

    private val consumerBackup by lazy { KafkaConsumer<TickerKey, CqlTicker>(
            configuration.tickersBackupConsumerConfig,
            JsonDeserializer(TickerKey::class.java),
            JsonDeserializer(CqlTicker::class.java)
    ).apply {
        subscribe(listOf(configuration.tickersBackupTopicName))
    }}

    private val producer by lazy { KafkaProducer<TickerKey, CqlTicker>(
            configuration.tickerProducerConfig,
            JsonSerializer<TickerKey>(),
            JsonSerializer<CqlTicker>()
    ).apply { initTransactions() }}

    private var firstPoll: Boolean = true

    fun poll(): ConsumerRecords<String, Trade> {
        if (firstPoll) {
            seekToEnd()
            firstPoll = false
        }
        return consumer.poll(configuration.pollTimeout)
    }

    fun saveAndProduceToKafka(tickers: MutableMap<TokensPair, MutableMap<String, MutableMap<Long, Ticker>>>, currentMillisHop: Long) {
        val tickerSnapshots = mutableListOf<Ticker>()
        val topicName = configuration.tickersTopicName

        producer.beginTransaction()
        try {
            tickers.forEach { _, exchangeMap ->
                exchangeMap.forEach { _, windowDurMap ->
                    windowDurMap.forEach { windowDuration, ticker ->
                        if (configuration.allowNotClosedWindows) {
                            producer.send(produceRecord(CqlTicker(ticker), topicName))
                        } else if (ticker.timestampTo!!.time <= currentMillisHop) {
                            producer.send(produceRecord(CqlTicker(ticker), topicName))
                        }
                        if (ticker.timestampTo!!.time <= currentMillisHop && isSnapshot(ticker, windowDuration)) {
                            tickerSnapshots.add(ticker)
                        }
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

    private fun saveSnapshots(tickerSnapshots: MutableList<Ticker>) {
        log.debug("Save tickers snapshots")

        val snapshots = mutableListOf<CqlTicker>()
        tickerSnapshots.forEach {
            snapshots.add(CqlTicker(it))
        }

        Schedulers.io().scheduleDirect {
            try {
                tickerRepository.saveAll(snapshots).collectList().block()
            } catch (e: Exception) {
                backupTickerToKafka(snapshots)
            }
        }

        restoreTickersFromKafka()
    }

    private fun backupTickerToKafka(tickers: MutableList<CqlTicker>) {
        log.debug("Backuping tickers to kafka: {}", tickers.size)

        producer.beginTransaction()
        try {
            tickers.forEach { ticker ->
                producer.send(produceRecord(ticker, configuration.tickersBackupTopicName))
            }
        } catch (e: Exception) {
            producer.abortTransaction()
            Runtime.getRuntime().exit(-1)
        }
        producer.commitTransaction()
    }

    private fun restoreTickersFromKafka() {
        log.debug("Restore tickers from kafka")

        Schedulers.single().scheduleDirect {
            val records = consumerBackup.poll(configuration.pollTimeout)
            log.debug("Tickers for restore count: {}", records.count())

            records
                    .forEach { cqlTickerRecord ->
                        try {
                            tickerRepository.save(cqlTickerRecord.value()).block()
                        } catch (e: Exception) {
                            log.debug("Restore failed: {} ", cqlTickerRecord)
                        }
                    }
        }
    }

    private fun isSnapshot(ticker: Ticker, windowDuration: Long): Boolean {
        return ticker.timestampTo!!.time % windowDuration == 0L
    }

    private fun produceRecord(ticker: CqlTicker, topicName: String): ProducerRecord<TickerKey, CqlTicker> {
        return ProducerRecord(
                topicName,
                TickerKey(TokensPair(ticker.pair.base, ticker.pair.quote), ticker.windowDuration, Timestamp(ticker.timestampTo!!.time)),
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

}