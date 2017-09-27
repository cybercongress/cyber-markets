package fund.cyber.markets.exchanges.bitfinex

import fund.cyber.markets.connectors.bitfinex.BitfinexTradesMessageParser
import fund.cyber.markets.connectors.common.ContainingUnknownTokensPairMessage
import fund.cyber.markets.connectors.common.TradesUpdatesMessage
import fund.cyber.markets.model.TokensPairInitializer
import fund.cyber.markets.model.Trade
import fund.cyber.markets.model.TradeType.SELL
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.math.BigDecimal

@DisplayName("Bitfinex Message Parser: ")
class BitfinexMessageParserTest {

    @Test
    @DisplayName("Should parse message into right trade")
    fun testParseTrade() {

        val message = """[53,"te",[43334639,1499972199000,-0.01293103,2320]]"""
        val tokensPair = TokensPairInitializer("BTC", "ETH")
        val tradesChannelSymbolForTokensPair = mapOf("tBTCETH" to tokensPair)
        val tradesChannelIdForTokensPair = mapOf(53 to tokensPair)
        val messageParser = BitfinexTradesMessageParser(tradesChannelSymbolForTokensPair, tradesChannelIdForTokensPair)

        val exchangeMessage = messageParser.parseMessage(message)
        assertTrue(exchangeMessage is TradesUpdatesMessage)
        assertTrue((exchangeMessage as TradesUpdatesMessage).trades.size == 1)

        val trade = Trade.of(
                tradeId = "43334639",
                exchange = "Bitfinex",
                timestamp = 1499972199,
                type = SELL,
                baseAmount = BigDecimal("0.01293103"),
                quoteAmount = BigDecimal("0.01293103") * BigDecimal("2320"),
                spotPrice = BigDecimal("2320"),
                tokensPairInitializer = tokensPair
        )
        assertEquals(trade, exchangeMessage.trades[0])
    }

    @Test
    @DisplayName("Should not parse due to containing unknown tokens pair")
    fun testParseMessageWithUnknownTokensPair() {

        val message = """[53,"te",[43334639,1499972199000,-0.01293103,2320]]"""
        val messageParser = BitfinexTradesMessageParser(emptyMap(), emptyMap())

        val exchangeMessage = messageParser.parseMessage(message)
        Assertions.assertTrue(exchangeMessage is ContainingUnknownTokensPairMessage)
        Assertions.assertEquals("53", (exchangeMessage as ContainingUnknownTokensPairMessage).symbol)
    }
}