package fund.cyber.markets.tickers

import fund.cyber.markets.cassandra.CassandraService
import fund.cyber.markets.tickers.configuration.TickersConfiguration
import fund.cyber.markets.tickers.service.TickerService
import fund.cyber.markets.tickers.service.VolumeService
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("TickersApplication")!!

fun main(args: Array<String>) {

    val configuration = TickersConfiguration()
    val cassandraService = CassandraService(configuration.cassandraProperties)
    val tickerService = TickerService(configuration, cassandraService)
    val volumeService = VolumeService(configuration, cassandraService)

    Runtime.getRuntime().addShutdownHook(object : Thread() {
        override fun run() {
            cassandraService.shutdown()
        }
    })

    try {
        TickersProcessor(
                configuration,
                tickerService,
                volumeService
        ).process()
    } catch (e: Exception) {
        log.error("An error occurred during execution TickersApplication", e)
        cassandraService.shutdown()
        Runtime.getRuntime().exit(-1)
    }
}