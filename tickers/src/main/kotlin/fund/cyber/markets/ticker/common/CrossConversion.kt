package fund.cyber.markets.ticker.common

import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.RoundingMode

private const val DECIMAL_SCALE = 20
private const val CONVERSION_SYMBOL = "BTC"

@Component
class CrossConversion(
        val prices: MutableMap<String, MutableMap<String, MutableMap<String, BigDecimal>>> = mutableMapOf()
) {

    private lateinit var base: String
    private lateinit var quote: String
    private lateinit var exchange: String

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

        return when {
            base == quote -> BigDecimal.ONE
            tryDirect() -> calcDirect()
            tryInvert() -> calcInvert()
            tryMultiply() -> calcMultiply()
            tryDivide() -> calcDivide()
            tryInvertDivide() -> calcInvertDivide()
            else -> null
        }
    }

    private fun tryDirect(): Boolean {
        val price = prices.get(base)?.get(quote)?.get(exchange)

        return price != null
    }

    private fun tryInvert(): Boolean {
        val price = prices.get(quote)?.get(base)?.get(exchange)

        return price != null
    }

    private fun tryMultiply(): Boolean {
        val price1 = prices.get(base)?.get(CONVERSION_SYMBOL)?.get(exchange)
        val price2 = prices.get(CONVERSION_SYMBOL)?.get(quote)?.get(exchange)

        return price1 != null && price2 != null
    }

    private fun tryDivide(): Boolean {
        val price1 = prices.get(base)?.get(CONVERSION_SYMBOL)?.get(exchange)
        val price2 = prices.get(quote)?.get(CONVERSION_SYMBOL)?.get(exchange)

        return price1 != null && price2 != null
    }

    private fun tryInvertDivide(): Boolean {
        val price1 = prices.get(CONVERSION_SYMBOL)?.get(quote)?.get(exchange)
        val price2 = prices.get(CONVERSION_SYMBOL)?.get(base)?.get(exchange)

        return price1 != null && price2 != null
    }

    private fun calcDirect(): BigDecimal {
        return prices.get(base)?.get(quote)?.get(exchange)!!
    }

    private fun calcInvert(): BigDecimal {
        val price = prices.get(quote)?.get(base)?.get(exchange)

        return BigDecimal.ONE.divide(price, DECIMAL_SCALE, RoundingMode.HALF_EVEN)
    }

    private fun calcMultiply(): BigDecimal {
        val price1 = prices.get(base)?.get(CONVERSION_SYMBOL)?.get(exchange)
        val price2 = prices.get(CONVERSION_SYMBOL)?.get(quote)?.get(exchange)

        return price1!!.multiply(price2)
    }

    private fun calcDivide(): BigDecimal {
        val price1 = prices.get(base)?.get(CONVERSION_SYMBOL)?.get(exchange)
        val price2 = prices.get(quote)?.get(CONVERSION_SYMBOL)?.get(exchange)

        return price1!!.div(price2!!)
    }

    private fun calcInvertDivide(): BigDecimal {
        val price1 = prices.get(CONVERSION_SYMBOL)?.get(quote)?.get(exchange)
        val price2 = prices.get(CONVERSION_SYMBOL)?.get(base)?.get(exchange)

        return price1!!.div(price2!!)
    }

}