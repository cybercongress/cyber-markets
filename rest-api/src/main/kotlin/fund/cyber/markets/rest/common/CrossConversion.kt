package fund.cyber.markets.rest.common

import fund.cyber.markets.cassandra.repository.TickerRepository
import fund.cyber.markets.dto.TokensPair
import fund.cyber.markets.model.Ticker
import fund.cyber.markets.rest.configuration.AppContext
import java.math.BigDecimal
import java.math.RoundingMode

private const val DECIMAL_SCALE = 20

class CrossConversion(
        val tickerRepository: TickerRepository = AppContext.tickerRepository,
        val base: String,
        val quote: String,
        val exchange: String,
        val windowDuration: Long,
        val timestamp: Long
) {
    /**
     * If the crypto currency does not trade directly from base to quote symbol BTC will be used for conversion
     * There is 4 methods of converting
     * - inverting (eg USD-BTC = 1/BTC-USD)
     * - multiplying through BTC (eg REP-USD = REP-BTC * BTC-USD)
     * - dividing through BTC (eg REP-XMR = REP-BTC/XMR-BTC)
     * - invert dividing through BTC if BTC trades in both pairs (eg USD-EUR = BTC-USD/BTC-EUR)
     */
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
        val ticker = tickerRepository.getTicker(TokensPair(quote, base), windowDuration, exchange, timestamp)

        return if (ticker != null) {
            ConversionResult(true, ConversionType.INVERT, calcInvert(ticker))
        } else{
            ConversionResult(false, null, null)
        }
    }

    private fun tryMultiply(): ConversionResult {
        val ticker1 = tickerRepository.getTicker(TokensPair(base, "BTC"), windowDuration, exchange, timestamp)
        val ticker2 = tickerRepository.getTicker(TokensPair("BTC", quote), windowDuration, exchange, timestamp)

        return if (ticker1 != null && ticker2 != null) {
            ConversionResult(true, ConversionType.MULTIPLY, calcMultiply(ticker1, ticker2))
        } else{
            ConversionResult(false, null, null)
        }
    }

    private fun tryDivide(): ConversionResult {
        val ticker1 = tickerRepository.getTicker(TokensPair(base, "BTC"), windowDuration, exchange, timestamp)
        val ticker2 = tickerRepository.getTicker(TokensPair(quote, "BTC"), windowDuration, exchange, timestamp)

        return if (ticker1 != null && ticker2 != null) {
            ConversionResult(true, ConversionType.DIVIDE, calcDivide(ticker1, ticker2))
        } else{
            ConversionResult(false, null, null)
        }
    }

    private fun tryInvertDivide(): ConversionResult {
        val ticker1 = tickerRepository.getTicker(TokensPair("BTC", quote), windowDuration, exchange, timestamp)
        val ticker2 = tickerRepository.getTicker(TokensPair("BTC", base), windowDuration, exchange, timestamp)

        return if (ticker1 != null && ticker2 != null) {
            ConversionResult(true, ConversionType.INVERT_DIVIDE, calcDivide(ticker1, ticker2))
        } else{
            ConversionResult(false, null, null)
        }
    }

    private fun calcInvert(ticker: Ticker): BigDecimal {
        return BigDecimal.ONE.divide(ticker.close, DECIMAL_SCALE, RoundingMode.HALF_EVEN)
    }

    private fun calcMultiply(ticker1: Ticker, ticker2: Ticker): BigDecimal {
        return ticker1.close.multiply(ticker2.close)
    }

    private fun calcDivide(ticker1: Ticker, ticker2: Ticker): BigDecimal {
        return ticker1.close.div(ticker2.close)
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