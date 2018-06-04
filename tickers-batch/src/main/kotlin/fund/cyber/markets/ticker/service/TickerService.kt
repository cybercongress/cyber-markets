package fund.cyber.markets.ticker.service

import fund.cyber.markets.cassandra.common.toTokenTicker
import fund.cyber.markets.cassandra.model.CqlTokenTicker
import fund.cyber.markets.cassandra.repository.TickerRepository
import fund.cyber.markets.common.model.TokenTicker
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import java.util.*

@Service
class TickerService(
    private val tickerRepository: TickerRepository
) {
    private val log = LoggerFactory.getLogger(TickerService::class.java)!!

    fun save(tickers : MutableCollection<TokenTicker>) {
        log.info("Saving tickers. Count: ${tickers.size}")

        tickerRepository.saveAll(tickers.map { CqlTokenTicker(it) }).collectList().block()
    }

    fun findTickersByInterval(symbol: String, timestampFrom: Long, timestampTo: Long, interval: Long): List<TokenTicker>  {

        var tickers = Flux.empty<CqlTokenTicker>()

        for (timestamp in timestampFrom..timestampTo step interval) {
            tickers = tickers.mergeWith(
                tickerRepository.find(symbol, Date(timestamp), interval)
            )
        }

        return tickers
            .collectList()
            .defaultIfEmpty(mutableListOf())
            .block()
            ?.map {
                it.toTokenTicker()
            }!!
    }

}