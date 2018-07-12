package fund.cyber.markets.ticker.service

import fund.cyber.markets.cassandra.model.CqlTokenTicker
import fund.cyber.markets.cassandra.repository.TickerRepository
import fund.cyber.markets.common.Intervals
import fund.cyber.markets.common.MILLIS_TO_DAYS
import fund.cyber.markets.common.convert
import fund.cyber.markets.common.model.TokenTicker
import org.ehcache.Cache
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

data class TickerCacheKey(
    val symbol: String,
    val timestampFrom: Long,
    val timestampTo: Long,
    val interval: Long
)

@Service
class TickerService(
    private val tickerRepository: TickerRepository,
    private val cache: Cache<TickerCacheKey, MutableList<CqlTokenTicker>>
) {
    private val log = LoggerFactory.getLogger(TickerService::class.java)!!

    fun save(tickers: MutableCollection<TokenTicker>): Flux<CqlTokenTicker> {
        log.info("Saving tickers. Count: ${tickers.size}")

        return tickerRepository.saveAll(tickers.map { CqlTokenTicker(it) })
    }

    fun findTickersByInterval(symbol: String, timestampFrom: Long, timestampTo: Long, interval: Long): Flux<CqlTokenTicker> {

        var tickers = Flux.empty<CqlTokenTicker>()

        val epochDay = timestampFrom convert MILLIS_TO_DAYS
        var timestampFromVar = timestampFrom

        if (interval <= Intervals.HOUR) {
            var timestampToVar = timestampFrom + Intervals.HOUR

            while (timestampTo - timestampToVar >= Intervals.HOUR) {

                tickers = tickers.mergeWith(
                    Flux.defer {
                        val cachedValue = cache.get(TickerCacheKey(symbol, timestampFromVar, timestampToVar, interval))
                            ?: mutableListOf()
                        when {
                            cachedValue.isNotEmpty() -> Flux.fromIterable(cachedValue)
                            else -> Flux.empty()
                        }
                    }.switchIfEmpty(
                        tickerRepository
                            .find(symbol, epochDay, Date(timestampFromVar), Date(timestampToVar), interval)
                            .flatMap { ticker ->
                                Mono.fromCallable {
                                    val key = TickerCacheKey(symbol, timestampFrom, timestampTo, interval)
                                    val list = cache.get(key)

                                    if (list == null) {
                                        cache.put(key, mutableListOf(ticker))
                                    } else {
                                        list.add(ticker)
                                    }
                                    ticker
                                }
                            }
                    )
                )
                timestampFromVar += Intervals.HOUR
                timestampToVar += Intervals.HOUR
            }

        }

        tickers = tickers.mergeWith(
            tickerRepository.find(symbol, epochDay, Date(timestampFromVar), Date(timestampTo), interval)
        )

        return tickers
    }

}