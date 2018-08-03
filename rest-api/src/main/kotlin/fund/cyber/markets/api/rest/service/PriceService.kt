package fund.cyber.markets.api.rest.service

import fund.cyber.markets.cassandra.common.toTokenTicker
import fund.cyber.markets.cassandra.repository.TickerRepository
import fund.cyber.markets.common.Intervals
import fund.cyber.markets.common.MILLIS_TO_DAYS
import fund.cyber.markets.common.closestSmallerMultiplyFromTs
import fund.cyber.markets.common.convert
import fund.cyber.markets.ticker.common.CrossConversion
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.core.publisher.toFlux
import java.math.BigDecimal
import java.util.*

@Service
class PriceService(
    private val tickerRepository: TickerRepository
) {

    fun getPrices(base: String, quotes: List<String>, exchange: String): Mono<Map<String, BigDecimal>> {

        val timestampFrom = Date(closestSmallerMultiplyFromTs(Intervals.MINUTE) - Intervals.MINUTE)
        val timestampTo = Date(closestSmallerMultiplyFromTs(Intervals.MINUTE))
        val epochDay = timestampFrom.time convert MILLIS_TO_DAYS

        return tickerRepository
            .find(base, epochDay, timestampFrom, timestampTo, Intervals.MINUTE)
            .mergeWith(
                quotes.toFlux().flatMap { quote ->
                    tickerRepository.find(quote, epochDay, timestampFrom, timestampTo, Intervals.MINUTE)
                }
            )
            .map { cqlTicker -> cqlTicker.toTokenTicker() }
            .collectList()
            .flatMap { tickersList ->
                Mono.fromCallable {
                    CrossConversion(tickersList).calculate(base, quotes, exchange)
                        .filterValues { value -> value != null }
                        .mapValues { entry -> entry.value!! }
                }
            }
    }

    fun getPrices(bases: List<String>, quotes: List<String>, exchange: String): Mono<Map<String, Map<String, BigDecimal>>> {

        return bases.toFlux()
            .flatMap { base -> getPrices(base, quotes, exchange).map { price -> base to price } }
            .collectList()
            .map { list -> list.toMap() }
    }

}