package fund.cyber.markets.ticker.common

import fund.cyber.markets.common.MILLIS_TO_MINUTES
import fund.cyber.markets.common.convert
import fund.cyber.markets.common.model.TokensPair
import fund.cyber.markets.common.model.Trade
import fund.cyber.markets.common.model.TradeType
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext


class CrossConversionTest {

    private lateinit var crossConversion: CrossConversion
    private val exchange = "TestEx"

    @Before
    fun before() {

        val ts = System.currentTimeMillis()
        val epochMin = ts convert MILLIS_TO_MINUTES

        val trades = mutableListOf(
            Trade(exchange, TokensPair("BTC", "USD"), TradeType.ASK, ts, epochMin, "id", BigDecimal(5L), BigDecimal(10L), BigDecimal(2L)),
            Trade(exchange, TokensPair("XMR", "USD"), TradeType.ASK, ts, epochMin, "id", BigDecimal(100L), BigDecimal(10L), BigDecimal(0.1)),
            Trade(exchange, TokensPair("LTC", "BTC"), TradeType.ASK, ts, epochMin, "id", BigDecimal(2L), BigDecimal(50L), BigDecimal(25)),
            Trade(exchange, TokensPair("BTC", "ETH"), TradeType.ASK, ts, epochMin, "id", BigDecimal(3L), BigDecimal(30L), BigDecimal(10))
        )

        crossConversion = CrossConversion().apply {
            updateMapOfPrices(trades)
        }
    }

    @Test
    fun equalBaseAndQuoteTest() {
        val price = crossConversion.calculate("BTC", "BTC", exchange)

        Assertions.assertThat(price).isEqualTo(BigDecimal.ONE)
    }

    @Test
    fun directTest() {
        val price = crossConversion.calculate("BTC", "USD", exchange)

        Assertions.assertThat(price).isEqualTo(BigDecimal(2L))
    }

    @Test
    fun invertTest() {
        val price = crossConversion.calculate("USD", "XMR", exchange)

        Assertions.assertThat(price!!.round(MathContext.DECIMAL32)).isEqualTo(BigDecimal(BigInteger.valueOf(1000000), 5))
    }

    @Test
    fun multiplyTest() {
        val price = crossConversion.calculate("LTC", "USD", exchange)

        Assertions.assertThat(price!!.round(MathContext.DECIMAL32)).isEqualTo(BigDecimal(50))
    }

    @Test
    fun divideTest() {
        val price = crossConversion.calculate("BTC", "XMR", exchange)

        Assertions.assertThat(price!!.round(MathContext.DECIMAL32)).isEqualTo(BigDecimal(20))
    }

    @Test
    fun invertDivideTest() {
        val price = crossConversion.calculate("USD", "ETH", exchange)

        Assertions.assertThat(price!!.round(MathContext.DECIMAL32)).isEqualTo(BigDecimal(5))
    }
}