package fund.cyber.markets.tickers.service

import fund.cyber.markets.cassandra.CassandraService
import fund.cyber.markets.dto.TokensPair
import fund.cyber.markets.kafka.JsonDeserializer
import fund.cyber.markets.kafka.JsonSerializer
import fund.cyber.markets.model.Ticker
import fund.cyber.markets.model.TickerKey
import fund.cyber.markets.model.Trade
import fund.cyber.markets.tickers.configuration.TickersConfiguration
import io.reactivex.schedulers.Schedulers
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.TopicPartition
import org.slf4j.LoggerFactory
import java.sql.Timestamp

class TickerService(val configuration: TickersConfiguration,
                    cassandraService: CassandraService) {

    private val log = LoggerFactory.getLogger(TickerService::class.java)!!
    private val tickerRepository = cassandraService.tickerRepository

    private val consumer = KafkaConsumer<String, Trade>(
            configuration.tickerConsumerConfig,
            JsonDeserializer(String::class.java),
            JsonDeserializer(Trade::class.java)
    )

    private val consumerBackup = KafkaConsumer<TickerKey, Ticker>(
            configuration.tickersBackupConsumerConfig,
            JsonDeserializer(TickerKey::class.java),
            JsonDeserializer(Ticker::class.java)
    )

    private val producer = KafkaProducer<TickerKey, Ticker>(
            configuration.tickerProducerConfig,
            JsonSerializer<TickerKey>(),
            JsonSerializer<Ticker>()
    ).apply { initTransactions() }

    init {
        consumer.subscribe(configuration.topicNamePattern)
        consumerBackup.subscribe(listOf(configuration.tickersBackupTopicName))
        seekToEnd()
    }

    fun poll(timeout: Long): ConsumerRecords<String, Trade> {
        return consumer.poll(timeout / 2)
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
                            producer.send(produceRecord(ticker, topicName))
                        } else if (ticker.timestampTo!!.time <= currentMillisHop) {
                            producer.send(produceRecord(ticker, topicName))
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

        val snapshots = mutableListOf<Ticker>()
        tickerSnapshots.forEach {
            snapshots.add(it.copy())
        }

        Schedulers.io().scheduleDirect {
            snapshots.forEach { ticker ->
                try {
                    tickerRepository.save(ticker)
                } catch (e: Exception) {
                    backupTickerToKafka(ticker)
                }
            }
        }

        restoreTickersFromKafka()
    }

    private fun backupTickerToKafka(ticker: Ticker) {
        log.debug("Backuping ticker to kafka: {}", ticker)

        producer.beginTransaction()
        try {
            producer.send(produceRecord(ticker, configuration.tickersBackupTopicName))
        } catch (e: Exception) {
            producer.abortTransaction()
            Runtime.getRuntime().exit(-1)
        }
        producer.commitTransaction()
    }

    private fun restoreTickersFromKafka() {
        log.debug("Restore tickers from kafka")

        Schedulers.single().scheduleDirect {
            val records: ConsumerRecords<TickerKey, Ticker> = consumerBackup.poll(configuration.windowHop / 2)
            log.debug("Tickers for restore count: {}", records.count())

            records.forEach { record ->
                try {
                    tickerRepository.save(record.value())
                } catch (e: Exception) {
                    log.debug("Restore failed: {} ", record.value())
                }
            }
        }
    }

    private fun isSnapshot(ticker: Ticker, windowDuration: Long): Boolean {
        return ticker.timestampTo!!.time % windowDuration == 0L
    }

    private fun produceRecord(ticker: Ticker, topicName: String): ProducerRecord<TickerKey, Ticker> {
        return ProducerRecord(
                topicName,
                TickerKey(ticker.pair, ticker.windowDuration, Timestamp(ticker.timestampTo!!.time)),
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