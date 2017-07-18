package fund.cyber.markets.exchanges.bitfinex

import fund.cyber.markets.webscoket.ContainingUnknownTokensPairMessage
import fund.cyber.markets.webscoket.TradesAndOrdersUpdatesMessage
import fund.cyber.markets.model.TokensPair
import fund.cyber.markets.model.Trade
import fund.cyber.markets.model.TradeType.SELL
import fund.cyber.markets.model.bitfinex
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
        val tokensPair = TokensPair("BTC", "ETH")
        val bitfinexMetaInfo = BitfinexMetaInformation()
        val messageParser = BitfinexMessageParser(bitfinexMetaInfo)
        bitfinexMetaInfo.tradesChannelIdForTokensPair.put(53, tokensPair)

        val exchangeMessage = messageParser.parseMessage(message)
        assertTrue(exchangeMessage is TradesAndOrdersUpdatesMessage)
        assertTrue((exchangeMessage as TradesAndOrdersUpdatesMessage).trades.size == 1)

        val trade = Trade(
                tradeId = "43334639", exchange = bitfinex,
                tokensPair = tokensPair, type = SELL,
                baseAmount = BigDecimal("0.01293103"), quoteAmount = BigDecimal("0.01293103") * BigDecimal("2320"),
                spotPrice = BigDecimal("2320"), timestamp = 1499972199
        )
        assertEquals(trade, exchangeMessage.trades[0])
    }

    @Test
    @DisplayName("Should not parse due to containing unknown tokens pair")
    fun testParseMessageWithUnknownTokensPair() {

        val message = """[53,"te",[43334639,1499972199000,-0.01293103,2320]]"""
        val bitfinexMetaInfo = BitfinexMetaInformation()
        val messageParser = BitfinexMessageParser(bitfinexMetaInfo)

        val exchangeMessage = messageParser.parseMessage(message)
        Assertions.assertTrue(exchangeMessage is ContainingUnknownTokensPairMessage)
        Assertions.assertEquals("53", (exchangeMessage as ContainingUnknownTokensPairMessage).symbol)
    }
}