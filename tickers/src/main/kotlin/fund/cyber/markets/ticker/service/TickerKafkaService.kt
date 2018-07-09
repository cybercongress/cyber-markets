package fund.cyber.markets.ticker.service

import fund.cyber.markets.cassandra.model.CqlTokenTicker
import fund.cyber.markets.common.model.TokenTickerKey
import fund.cyber.markets.common.model.Trade
import fund.cyber.markets.ticker.configuration.TICKERS_BACKUP_TOPIC_NAME
import fund.cyber.markets.ticker.configuration.TICKERS_TOPIC_NAME
import io.reactivex.schedulers.Schedulers
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.TopicPartition
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.sql.Timestamp
import javax.annotation.PostConstruct

@Service
class TickerKafkaService(
    private val tickerProducer: KafkaProducer<TokenTickerKey, CqlTokenTicker>,
    private val tradesConsumer: KafkaConsumer<String, Trade>,
    private val tickersBackupConsumer: KafkaConsumer<TokenTickerKey, CqlTokenTicker>
) {

    private val log = LoggerFactory.getLogger(TickerKafkaService::class.java)!!

    //todo: restore state after start. issue #137
    @PostConstruct
    fun skipOldTrades() {
        seekToEnd()
    }

    fun pollTrades(timeout: Long): ConsumerRecords<String, Trade> {
        return tradesConsumer.poll(timeout)
    }

    fun send(tickers: MutableList<CqlTokenTicker>) {
        Schedulers.io().scheduleDirect {
            try {
                tickers.forEach { ticker ->
                    tickerProducer.send(ProducerRecord(
                        TICKERS_TOPIC_NAME,
                        TokenTickerKey(ticker.symbol, ticker.interval, Timestamp(ticker.timestampTo.time)),
                        ticker))
                }
            } catch (e: Exception) {
                log.error("Cannot produce ticker to kafka. Exiting app", e)
                Runtime.getRuntime().exit(-1)
            }
        }
    }

    fun backupTickers(tickers: MutableList<CqlTokenTicker>) {
        log.info("Backuping tickers to kafka: {}", tickers.size)

        try {
            tickers.forEach { ticker ->
                tickerProducer.send(ProducerRecord(
                    TICKERS_BACKUP_TOPIC_NAME,
                    TokenTickerKey(ticker.symbol, ticker.interval, Timestamp(ticker.timestampTo.time)),
                    ticker))
            }
        } catch (e: Exception) {
            log.error("Tickers backup to kafka failed. Exiting app", e)
            Runtime.getRuntime().exit(-1)
        }
    }

    fun pollBackupedTickers(timeout: Long): List<CqlTokenTicker> {
        val records = tickersBackupConsumer.poll(timeout)
        log.info("Tickers for restore count: {}", records.count())

        return records.map { it.value() }
    }

    private fun seekToEnd() {
        tradesConsumer.poll(0)
        val partitions = mutableListOf<TopicPartition>()
        val tradeTopics = tradesConsumer.subscription()
        tradeTopics.forEach { topic ->
            tradesConsumer.partitionsFor(topic).forEach { partitionInfo ->
                partitions.add(TopicPartition(partitionInfo.topic(), partitionInfo.partition()))
            }
        }
        tradesConsumer.seekToEnd(partitions)
    }

}