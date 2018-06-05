package fund.cyber.markets.ticker.processor.price

import fund.cyber.markets.common.model.Exchanges
import fund.cyber.markets.common.model.TokenPrice
import fund.cyber.markets.common.model.TokenTicker
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.RoundingMode

@Component
class WeightedPriceProcessor(
    override val methodName: String = "WeightedPrice"
) : PriceProcessor {

    override fun calculate(tickers: List<TokenTicker>): List<TokenPrice> {

        val prices = mutableListOf<TokenPrice>()

        tickers.forEach { ticker ->

            val price = TokenPrice(
                symbol = ticker.symbol,
                method = methodName,
                timestampTo = ticker.timestampTo
            )

            ticker.price.forEach { baseTokenSymbol, exchangeMap ->

                var priceValue = BigDecimal.ZERO

                exchangeMap.forEach { exchange, tickerPrice ->

                    val closePrice = tickerPrice.close
                    if (closePrice > BigDecimal.ZERO) {

                        try {
                            priceValue = priceValue.plus(
                                tickerPrice.close.multiply(
                                    ticker.baseVolume[baseTokenSymbol]!![exchange]
                                ).divide(
                                    ticker.baseVolume[baseTokenSymbol]!![Exchanges.ALL],
                                    RoundingMode.HALF_EVEN
                                )
                            )
                        } catch (e: Exception) {
                            e.toString()
                        }

                    }

                }

                price.values
                    .getOrPut(Exchanges.ALL, { mutableMapOf() })
                    .put(baseTokenSymbol, priceValue)
            }

            prices.add(price)
        }

        return prices
    }

}