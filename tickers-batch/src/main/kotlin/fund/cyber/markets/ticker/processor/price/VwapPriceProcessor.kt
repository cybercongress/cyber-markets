package fund.cyber.markets.ticker.processor.price

import fund.cyber.markets.common.Durations
import fund.cyber.markets.common.closestSmallerMultiply
import fund.cyber.markets.common.model.TokenPrice
import fund.cyber.markets.common.model.TokenTicker
import fund.cyber.markets.ticker.service.TickerService
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class VwapPriceProcessor(
    override val methodName: String = "VWAP"
) : PriceProcessor {

    private lateinit var tickerService: TickerService

    override fun calculate(tickers: List<TokenTicker>): List<TokenPrice> {

        val prices = mutableListOf<TokenPrice>()

        tickers.forEach { ticker ->

            // get tickers from epoch 00:00
            val symbol = ticker.symbol
            val timestampFrom = closestSmallerMultiply(ticker.timestampFrom, Durations.DAY)
            val timestampTo = ticker.timestampTo - ticker.interval
            val interval = ticker.interval


            val previousTickers = tickerService.findTickersByInterval(symbol, timestampFrom, timestampTo, interval)
            previousTickers.toMutableList().add(ticker)

            val pvMap = getTotalPV(previousTickers)

            val tokenPrice = TokenPrice(
                symbol = symbol,
                method = methodName,
                timestampFrom = ticker.timestampFrom,
                timestampTo = ticker.timestampTo
            )

            pvMap.forEach { exchange, baseTokenSymbolMap ->
                baseTokenSymbolMap.forEach { baseTokenSymbol, pv ->

                    val vwapPrice = pv.totalPV.divide(pv.totalVolume)

                    tokenPrice.values
                        .getOrPut(exchange, { mutableMapOf() })
                        .put(baseTokenSymbol, vwapPrice)
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