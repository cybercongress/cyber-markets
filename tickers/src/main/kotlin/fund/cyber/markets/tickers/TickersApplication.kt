package fund.cyber.markets.tickers

import fund.cyber.markets.tickers.processor.MainProcessor
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("TickersApplication")!!

fun main(args: Array<String>) {

    Runtime.getRuntime().addShutdownHook(object : Thread() {
        override fun run() {
            AppContext.cassandraService.shutdown()
        }
    })

    try {
        MainProcessor().process()
    } catch (e: Exception) {
        log.error("An error occurred during execution TickersApplication", e)
        AppContext.cassandraService.shutdown()
        Runtime.getRuntime().exit(-1)
    }

}