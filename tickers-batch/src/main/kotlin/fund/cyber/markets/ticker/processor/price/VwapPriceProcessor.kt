package fund.cyber.markets.ticker.processor.price

import fund.cyber.markets.cassandra.common.toTokenTicker
import fund.cyber.markets.common.Intervals
import fund.cyber.markets.common.closestSmallerMultiply
import fund.cyber.markets.common.model.TokenPrice
import fund.cyber.markets.common.model.TokenTicker
import fund.cyber.markets.ticker.processor.HistoricalTickerProcessor
import fund.cyber.markets.ticker.service.TickerService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.toMono
import java.math.BigDecimal
import java.math.RoundingMode

@Component
class VwapPriceProcessor(
    override val methodName: String = "VWAP"
) : PriceProcessor {
    private val log = LoggerFactory.getLogger(HistoricalTickerProcessor::class.java)!!

    @Autowired
    private lateinit var tickerService: TickerService

    override fun calculate(tickers: List<TokenTicker>): List<TokenPrice> {

        val prices = mutableListOf<TokenPrice>()

        tickers.forEach { ticker ->

            // get tickers from epoch 00:00
            val symbol = ticker.symbol
            val timestampFrom = closestSmallerMultiply(ticker.timestampFrom, Intervals.DAY)
            val timestampTo = ticker.timestampTo - ticker.interval
            val interval = ticker.interval

            log.debug("Getting previous tickers for ${ticker.symbol}")

            val previousTickers = Flux.merge(
                ticker.toMono(),
                tickerService
                    .findTickersByInterval(symbol, timestampFrom, timestampTo, interval)
                    .map {
                        cqlTokenTicker -> cqlTokenTicker.toTokenTicker()
                    }
            )
                .collectList()
                .block()!!

            val pvMap = getTotalPV(previousTickers)

            val tokenPrice = TokenPrice(
                symbol = symbol,
                method = methodName,
                timestampFrom = ticker.timestampFrom,
                timestampTo = ticker.timestampTo
            )

            pvMap.forEach { exchange, baseTokenSymbolMap ->
                baseTokenSymbolMap.forEach { baseTokenSymbol, pv ->

                    try {
                        val vwapPrice = pv.totalPV.divide(pv.totalVolume, RoundingMode.HALF_EVEN)

                        tokenPrice.values
                            .getOrPut(exchange, { mutableMapOf() })
                            .put(baseTokenSymbol, vwapPrice)
                    } catch (e: Exception) {
                        log.warn("Cannot divide in $methodName price processor")
                    }
                }
            }

            prices.add(tokenPrice)
        }

        return prices
    }

    private fun getTotalPV(previousTickers: List<TokenTicker>): MutableMap<String, MutableMap<String, PV>> {

        // exchange -> base token symbol -> PV
        val pvMap = mutableMapOf<String, MutableMap<String, PV>>()

        previousTickers.forEach { ticker ->

            ticker.price.forEach { baseTokenSymbol, exchangeMap ->
                exchangeMap.forEach { exchange, tickerPrice ->

                    val typicalPrice = tickerPrice.close
                        .plus(tickerPrice.max)
                        .plus(tickerPrice.min)
                        .divide(BigDecimal(3L))

                    val volume = ticker.volume[baseTokenSymbol]?.get(exchange)

                    if (volume != null && typicalPrice > BigDecimal.ZERO) {

                        val pv = pvMap
                            .getOrPut(exchange, { mutableMapOf() })
                            .getOrPut(baseTokenSymbol, { PV() })

                        pv.totalVolume = pv.totalVolume.plus(volume)
                        pv.totalPV = pv.totalPV.plus(volume.multiply(typicalPrice))
                    }
                }
            }
        }

        return pvMap
    }

    private class PV(
        var totalVolume: BigDecimal = BigDecimal.ZERO,
        var totalPV: BigDecimal = BigDecimal.ZERO
    )

}