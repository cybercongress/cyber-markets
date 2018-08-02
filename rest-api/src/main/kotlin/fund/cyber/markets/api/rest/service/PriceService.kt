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
import reactor.core.publisher.toMono
import java.math.BigDecimal
import java.util.*

@Service
class PriceService(
    private val tickerRepository: TickerRepository
) {

    fun getPrices(base: String, quoutes: List<String>, exchange: String): Mono<Map<String, BigDecimal>> {

        val timestampFrom = Date(closestSmallerMultiplyFromTs(Intervals.MINUTE) - Intervals.MINUTE)
        val timestampTo = Date(closestSmallerMultiplyFromTs(Intervals.MINUTE))
        val epochDay = timestampFrom.time convert MILLIS_TO_DAYS

        var tickersFlux = tickerRepository
            .find(base, epochDay, timestampFrom, timestampTo, Intervals.MINUTE)

        quoutes.forEach { quote ->
            tickersFlux = tickersFlux
                .mergeWith(tickerRepository.find(quote, epochDay, timestampFrom, timestampTo, Intervals.MINUTE))
        }

        return tickersFlux
            .map { cqlTicker -> cqlTicker.toTokenTicker() }
            .collectList()
            .flatMap { tickers ->

                val crossConversion = CrossConversion()
                tickers.forEach { ticker ->
                    crossConversion.updateMapOfPrices(ticker)
                }

                val prices = mutableMapOf<String, BigDecimal>()

                quoutes.forEach { quote ->
                    val price = crossConversion.calculate(base, quote, exchange)

                    if (price != null) {
                        prices[quote] = price
                    }
                }

                prices.toMono()
            }
    }

    fun getPrices(bases: List<String>, quoutes: List<String>, exchange: String): Mono<Map<String, Map<String, BigDecimal>>> {

        return bases
            .toFlux()
            .flatMap { base ->
                Mono
                    .defer { base.toMono() }
                    .zipWith(getPrices(base, quoutes, exchange))
                    .map { result -> result.t1 to result.t2 }
            }
            .collectList()
            .map { list -> list.toMap() }
    }

}