package fund.cyber.markets.rest.common

import fund.cyber.markets.dao.service.TickerDaoService
import fund.cyber.markets.model.Ticker
import fund.cyber.markets.rest.configuration.AppContext
import java.math.BigDecimal

class CrossConversion(
        val tickerDaoService: TickerDaoService = AppContext.tickerDaoService,
        val base: String,
        val quote: String,
        val exchange: String,
        val windowDuration: Long,
        val timestamp: Long
) {

    fun calculate(): ConversionResult {
        val invert = tryInvert()
        if (invert.success) {
            return invert
        }
        val multiply = tryMultiply()
        if (multiply.success) {
            return multiply
        }
        val divide = tryDivide()
        if (divide.success) {
            return divide
        }
        val invertDivide = tryInvertDivide()
        if (invertDivide.success) {
            return invertDivide
        }

        return ConversionResult(false, null, null)
    }

    private fun tryInvert(): ConversionResult {
        val ticker = tickerDaoService.getTicker(quote, base, windowDuration, exchange, timestamp)

        return if (ticker != null) {
            ConversionResult(true, ConversionType.INVERT, calcInvert(ticker))
        } else{
            ConversionResult(false, null, null)
        }
    }

    private fun tryMultiply(): ConversionResult {
        val ticker1 = tickerDaoService.getTicker(base, "BTC", windowDuration, exchange, timestamp)
        val ticker2 = tickerDaoService.getTicker("BTC", quote, windowDuration, exchange, timestamp)

        return if (ticker1 != null && ticker2 != null) {
            ConversionResult(true, ConversionType.MULTIPLY, calcMultiply(ticker1, ticker2))
        } else{
            ConversionResult(false, null, null)
        }
    }

    private fun tryDivide(): ConversionResult {
        val ticker1 = tickerDaoService.getTicker(base, "BTC", windowDuration, exchange, timestamp)
        val ticker2 = tickerDaoService.getTicker(quote, "BTC", windowDuration, exchange, timestamp)

        return if (ticker1 != null && ticker2 != null) {
            ConversionResult(true, ConversionType.DIVIDE, calcDivide(ticker1, ticker2))
        } else{
            ConversionResult(false, null, null)
        }
    }

    private fun tryInvertDivide(): ConversionResult {
        val ticker1 = tickerDaoService.getTicker("BTC", quote, windowDuration, exchange, timestamp)
        val ticker2 = tickerDaoService.getTicker("BTC", base, windowDuration, exchange, timestamp)

        return if (ticker1 != null && ticker2 != null) {
            ConversionResult(true, ConversionType.INVERT_DIVIDE, calcDivide(ticker1, ticker2))
        } else{
            ConversionResult(false, null, null)
        }
    }

    private fun calcInvert(ticker: Ticker): BigDecimal {
        return BigDecimal(1).divide(ticker.price)
    }

    private fun calcMultiply(ticker1: Ticker, ticker2: Ticker): BigDecimal {
        return ticker1.price.multiply(ticker2.price)
    }

    private fun calcDivide(ticker1: Ticker, ticker2: Ticker): BigDecimal {
        return ticker1.price.divide(ticker2.price)
    }

}

data class ConversionResult(
        val success: Boolean,
        val type: ConversionType?,
        val value: BigDecimal?
)

enum class ConversionType {
    DIRECT,
    INVERT,
    MULTIPLY,
    DIVIDE,
    INVERT_DIVIDE
}