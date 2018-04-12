package fund.cyber.markets.ticker.service

import fund.cyber.markets.cassandra.model.CqlTokenTicker
import fund.cyber.markets.cassandra.repository.TickerRepository
import fund.cyber.markets.common.model.TokenTicker
import fund.cyber.markets.common.model.Trade
import fund.cyber.markets.ticker.configuration.TickersConfiguration
import io.reactivex.schedulers.Schedulers
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class TickerServiceImpl : TickerService {

    private val log = LoggerFactory.getLogger(TickerServiceImpl::class.java)!!

    @Autowired
    private lateinit var tickerRepository: TickerRepository
    @Autowired
    private lateinit var tickerKafkaService: TickerKafkaServiceImpl
    @Autowired
    private lateinit var configuration: TickersConfiguration

    private var isCassandraAlive = true
    private var restoreNeeded = false

    override fun poll(): ConsumerRecords<String, Trade> {
        return tickerKafkaService.pollTrades(configuration.pollTimeout)
    }

    override fun persist(tickers: MutableMap<String, MutableMap<Long, TokenTicker>>, currentHopFromMillis: Long) {
        val tickerSnapshots = mutableListOf<CqlTokenTicker>()
        val tickerClosed = mutableListOf<CqlTokenTicker>()

        tickers.forEach { _, windowDurationMap ->
            windowDurationMap.forEach { windowDuration, ticker ->
                val isClosedWindow = ticker.timestampTo <= currentHopFromMillis
                val isSnapshot = ticker.timestampTo % windowDuration == 0L

                if (isClosedWindow || configuration.allowNotClosedWindows) {
                    tickerClosed.add(CqlTokenTicker(ticker))
                }
                if (isClosedWindow && isSnapshot) {
                    tickerSnapshots.add(CqlTokenTicker(ticker))
                }
            }
        }

        if (tickerClosed.isNotEmpty()) {
            tickerKafkaService.send(tickerClosed)
        }
        if (tickerSnapshots.isNotEmpty()) {
            saveSnapshots(tickerSnapshots)
        }
    }

    private fun saveSnapshots(snapshots: MutableList<CqlTokenTicker>) {
        log.debug("Save tickers snapshots")

        Schedulers.single().scheduleDirect {
            try {
                tickerRepository.saveAll(snapshots)
                isCassandraAlive = true
            } catch (e: Exception) {
                log.error("Save tickers snapshots failed", e)
                restoreNeeded = true
                isCassandraAlive = false
                tickerKafkaService.backupTickers(snapshots)
            }
        }

        if (isCassandraAlive && restoreNeeded) {
            restoreTickersFromKafka()
        }
    }

    private fun restoreTickersFromKafka() {
        log.info("Restoring tickers from kafka")

        Schedulers.single().scheduleDirect {
            val tickers = tickerKafkaService.pollBackupedTickers(configuration.pollTimeout)

            try {
                tickerRepository.saveAll(tickers)
            } catch (e: Exception) {
                log.error("Tickers restore failed")
            }

            restoreNeeded = false
        }
    }

}