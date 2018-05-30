package fund.cyber.markets.ticker.processor.price

import fund.cyber.markets.common.model.Exchanges
import fund.cyber.markets.common.model.TokenPrice
import fund.cyber.markets.common.model.TokenTicker
import org.springframework.stereotype.Component
import java.math.BigDecimal

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

                        priceValue = priceValue.plus(
                            tickerPrice.close.multiply(
                                ticker.baseVolume[baseTokenSymbol]!![exchange]
                            ).divide(
                                ticker.baseVolume[baseTokenSymbol]!![Exchanges.ALL]
                            )
                        )

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