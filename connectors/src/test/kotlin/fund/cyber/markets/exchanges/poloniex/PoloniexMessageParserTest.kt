package fund.cyber.markets.exchanges.poloniex

import fund.cyber.markets.connectors.common.ContainingUnknownTokensPairMessage
import fund.cyber.markets.connectors.common.TradesUpdatesMessage
import fund.cyber.markets.connectors.poloniex.PoloniexTradesMessageParser
import fund.cyber.markets.model.TokensPair
import fund.cyber.markets.model.Trade
import fund.cyber.markets.model.TradeType
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.math.BigDecimal


@DisplayName("Poloniex Message Parser: ")
class PoloniexMessageParserTest {

    @Test
    @DisplayName("Should parse message into right trade")
    fun testParseTrade() {

        val message = """
        [129, 4679255,
          [
            ["o",0,"0.00003328","0.00000000"],
            ["t","126320",1,"0.00003328","399377.76875000",1499708547],
            ["t","126321",0,"0.00023328","2.76875000",1499708549]
          ]
        ]"""

        val tokensPair = TokensPair("BTC", "ETH")
        val channelIdForTokensPairs = mapOf(Pair("129", tokensPair))
        val messageParser = PoloniexTradesMessageParser(channelIdForTokensPairs)

        val exchangeMessage = messageParser.parseMessage(message)
        Assertions.assertTrue(exchangeMessage is TradesUpdatesMessage)
        Assertions.assertTrue((exchangeMessage as TradesUpdatesMessage).trades.size == 2)

        val firstTrade = Trade(
                tradeId = "126320",
                exchange = "Poloniex",
                timestamp = 1499708547,
                type = TradeType.BUY,
                baseAmount = BigDecimal("399377.76875000"),
                quoteAmount = BigDecimal("13.2912921440000000"),
                spotPrice = BigDecimal("0.00003328"),
                tokensPair = tokensPair
        )
        val secondTrade = Trade(
                tradeId = "126321",
                exchange = "Poloniex",
                timestamp = 1499708549,
                type = TradeType.SELL,
                baseAmount = BigDecimal("2.76875000"),
                quoteAmount = BigDecimal("0.0006458940000000"),
                spotPrice = BigDecimal("0.00023328"),
                tokensPair = tokensPair
        )
        Assertions.assertEquals(firstTrade, exchangeMessage.trades[0])
        Assertions.assertEquals(secondTrade, exchangeMessage.trades[1])
    }

    @Test
    @DisplayName("Should not parse due to containing unknown tokens pair")
    fun testParseMessageWithUnknownTokensPair() {

        val message = """[53,"te",[43334639,1499972199000,-0.01293103,2320]]"""
        val messageParser = PoloniexTradesMessageParser(emptyMap())

        val exchangeMessage = messageParser.parseMessage(message)
        Assertions.assertTrue(exchangeMessage is ContainingUnknownTokensPairMessage)
        Assertions.assertEquals("53", (exchangeMessage as ContainingUnknownTokensPairMessage).symbol)
    }
}