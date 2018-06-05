package fund.cyber.markets.ticker.service

import fund.cyber.markets.cassandra.common.toTokenTicker
import fund.cyber.markets.cassandra.model.CqlTokenTicker
import fund.cyber.markets.cassandra.repository.TickerRepository
import fund.cyber.markets.common.MILLIS_TO_DAYS
import fund.cyber.markets.common.convert
import fund.cyber.markets.common.model.TokenTicker
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*

@Service
class TickerService(
    private val tickerRepository: TickerRepository
) {
    private val log = LoggerFactory.getLogger(TickerService::class.java)!!

    fun save(tickers: MutableCollection<TokenTicker>) {
        log.info("Saving tickers. Count: ${tickers.size}")

        tickerRepository.saveAll(tickers.map { CqlTokenTicker(it) }).collectList().block()
    }

    fun findTickersByInterval(symbol: String, timestampFrom: Long, timestampTo: Long, interval: Long): MutableList<TokenTicker> {

        return tickerRepository.find(
            tokenSymbol = symbol,
            epochDay = timestampFrom convert MILLIS_TO_DAYS,
            timestampFrom = Date(timestampFrom),
            timestampTo = Date(timestampTo),
            interval = interval
        )
            .collectList()
            .doOnError { throwable ->
                log.error("Cannot get tickers for symbol: $symbol ", throwable)
            }
            .defaultIfEmpty(mutableListOf())
            .block()
            ?.map {
                it.toTokenTicker()
            }
            ?.toMutableList()!!
    }

}