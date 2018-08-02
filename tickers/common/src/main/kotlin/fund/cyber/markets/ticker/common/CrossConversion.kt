package fund.cyber.markets.ticker.common

import fund.cyber.markets.common.model.BaseTokens
import fund.cyber.markets.common.model.TokenTicker
import fund.cyber.markets.common.model.Trade
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.RoundingMode

private const val DECIMAL_SCALE = 20

@Component
class CrossConversion(
    val prices: MutableMap<String, MutableMap<String, MutableMap<String, BigDecimal>>> = mutableMapOf()
) {

    private lateinit var base: String
    private lateinit var quote: String
    private lateinit var exchange: String

    private val conversionSymbols = BaseTokens.values().map { it.name }

    fun updateMapOfPrices(trades: List<Trade>) {
        invalidatePrices()
        trades.forEach { trade ->
            prices
                .getOrPut(trade.pair.base) { mutableMapOf() }
                .getOrPut(trade.pair.quote) { mutableMapOf() }
                .put(trade.exchange, trade.price)
        }
    }

    fun updateMapOfPrices(ticker: TokenTicker) {
        ticker.price.forEach { baseTokenSymbol, exchangeMap ->
            exchangeMap.forEach { exchange, tickerPrice ->
                prices
                    .getOrPut(ticker.symbol) { mutableMapOf() }
                    .getOrPut(baseTokenSymbol) { mutableMapOf() }
                    .put(exchange, tickerPrice.close)
            }
        }

    }

    /**
     * If the crypto currency does not trade directly from base to quote symbol BTC will be used for conversion
     * There is 4 methods of converting
     * - inverting (eg USD-BTC = 1/BTC-USD)
     * - multiplying through BTC (eg REP-USD = REP-BTC * BTC-USD)
     * - dividing through BTC (eg REP-XMR = REP-BTC/XMR-BTC)
     * - invert dividing through BTC if BTC trades in both pairs (eg USD-EUR = BTC-USD/BTC-EUR)
     */
    fun calculate(base: String, quote: String, exchange: String): BigDecimal? {

        this.base = base
        this.quote = quote
        this.exchange = exchange

        var result = when {
            base == quote -> BigDecimal.ONE
            tryDirect() -> calcDirect()
            tryInvert() -> calcInvert()
            else -> null
        }

        if (result != null) {
            return result
        } else {

            conversionSymbols.forEach { symbol ->
                result = when {
                    tryMultiply(symbol) -> calcMultiply(symbol)
                    tryDivide(symbol) -> calcDivide(symbol)
                    tryInvertDivide(symbol) -> calcInvertDivide(symbol)
                    else -> null
                }

                if (result != null) {
                    return result
                }
            }

        }

        return null
    }

    fun invalidatePrices() {
        prices.clear()
    }

    private fun tryDirect(): Boolean {
        val price = prices.get(base)?.get(quote)?.get(exchange)

        return price != null
    }

    private fun tryInvert(): Boolean {
        val price = prices.get(quote)?.get(base)?.get(exchange)

        return price != null
    }

    private fun tryMultiply(conversionSymbol: String): Boolean {
        val price1 = prices.get(base)?.get(conversionSymbol)?.get(exchange)
        val price2 = prices.get(conversionSymbol)?.get(quote)?.get(exchange)

        return price1 != null && price2 != null
    }

    private fun tryDivide(conversionSymbol: String): Boolean {
        val price1 = prices.get(base)?.get(conversionSymbol)?.get(exchange)
        val price2 = prices.get(quote)?.get(conversionSymbol)?.get(exchange)

        return price1 != null && price2 != null
    }

    private fun tryInvertDivide(conversionSymbol: String): Boolean {
        val price1 = prices.get(conversionSymbol)?.get(quote)?.get(exchange)
        val price2 = prices.get(conversionSymbol)?.get(base)?.get(exchange)

        return price1 != null && price2 != null
    }

    private fun calcDirect(): BigDecimal {
        return prices.get(base)?.get(quote)?.get(exchange)!!
    }

    private fun calcInvert(): BigDecimal {
        val price = prices.get(quote)?.get(base)?.get(exchange)

        return BigDecimal.ONE.divide(price, DECIMAL_SCALE, RoundingMode.HALF_EVEN)
    }

    private fun calcMultiply(conversionSymbol: String): BigDecimal {
        val price1 = prices.get(base)?.get(conversionSymbol)?.get(exchange)
        val price2 = prices.get(conversionSymbol)?.get(quote)?.get(exchange)

        return price1!!.multiply(price2)
    }

    private fun calcDivide(conversionSymbol: String): BigDecimal {
        val price1 = prices.get(base)?.get(conversionSymbol)?.get(exchange)
        val price2 = prices.get(quote)?.get(conversionSymbol)?.get(exchange)

        return price1!!.div(price2!!)
    }

    private fun calcInvertDivide(conversionSymbol: String): BigDecimal {
        val price1 = prices.get(conversionSymbol)?.get(quote)?.get(exchange)
        val price2 = prices.get(conversionSymbol)?.get(base)?.get(exchange)

        return price1!!.div(price2!!)
    }

}