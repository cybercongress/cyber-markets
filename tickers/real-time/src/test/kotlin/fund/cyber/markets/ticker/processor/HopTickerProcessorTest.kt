package fund.cyber.markets.ticker.processor

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import fund.cyber.markets.common.MILLIS_TO_MINUTES
import fund.cyber.markets.common.convert
import fund.cyber.markets.common.model.TokensPair
import fund.cyber.markets.common.model.Trade
import fund.cyber.markets.common.model.TradeType
import fund.cyber.markets.ticker.common.CrossConversion
import fund.cyber.markets.ticker.service.TickerService
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal


class HopTickerProcessorTest {

    private lateinit var hopTickerProcessor: HopTickerProcessor
    private val windowHop = 3000L

    @Before
    fun before() {

        val ts = System.currentTimeMillis()
        val epochMin = ts convert MILLIS_TO_MINUTES

        val trades = mutableListOf(
            Trade("TestEx", TokensPair("BTC", "USD"), TradeType.ASK, ts, epochMin, "id", BigDecimal(5L), BigDecimal(10L), BigDecimal(2L)),
            Trade("TestEx", TokensPair("XMR", "USD"), TradeType.ASK, ts, epochMin, "id", BigDecimal(100L), BigDecimal(10L), BigDecimal(0.1))
        )

        val tickerService: TickerService = mock {
            on {
                pollTrades()
            }.doReturn(
                trades
            )
        }

        val crossConversion = CrossConversion()
        hopTickerProcessor = HopTickerProcessor(tickerService, crossConversion, windowHop)
    }

    @Test
    fun hopTickersTest() {

        val tickers = hopTickerProcessor.getHopTickers()

        Assertions.assertThat(tickers).hasSize(3)
        Assertions.assertThat(tickers["XMR"]?.baseVolume).hasSize(2)
        Assertions.assertThat(tickers["XMR"]!!.baseVolume["USD"]!!["ALL"]).isNotNull()
    }

}