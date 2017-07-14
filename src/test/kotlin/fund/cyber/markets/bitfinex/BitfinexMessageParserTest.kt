package fund.cyber.markets.bitfinex

import fund.cyber.markets.model.CurrencyPair
import fund.cyber.markets.model.ExchangeItemsReceivedMessage
import fund.cyber.markets.model.TradeType.SELL
import fund.cyber.markets.model.bitfinex
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.math.BigDecimal


class BitfinexMessageParserTest {

    val bitfinexMetaInfo = BitfinexMetaInformation()
    val messageParser = BitfinexMessageParser(bitfinexMetaInfo)

    @Test
    @DisplayName("Should not throw exception for invalid provided json, but return null")
    fun testInvalidJsonProvided() {
        val exchangeMessage = messageParser.parseMessage("[q34342%&$&__~~~~")
        assertNull(exchangeMessage)
    }

    @Test
    @DisplayName("Should not throw exception for unknown provided json, but return null")
    fun testUnknownJsonStructureProvided() {
        val exchangeMessage = messageParser.parseMessage("{\"event\":\"subscribed\",\"symbol\":\"tBTCUSD\"}")
        assertNull(exchangeMessage)
    }

    @Test
    @DisplayName("Should parse message into right trade")
    fun testParseTrade() {

        val currencyPair = CurrencyPair("BTC", "ETH")
        bitfinexMetaInfo.tradesChannelIdForCurrencyPair.put(53, currencyPair)

        val exchangeMessage = messageParser.parseMessage("[53,\"te\",[43334639,1499972199000,-0.01293103,2320]]")
        assertTrue(exchangeMessage is ExchangeItemsReceivedMessage)
        assertTrue((exchangeMessage as ExchangeItemsReceivedMessage).trades.size == 1)

        val trade = exchangeMessage.trades[0]
        assertEquals("43334639", trade.id)
        assertEquals(bitfinex, trade.exchange)
        assertEquals(1499972199, trade.timestamp)
        assertEquals(SELL, trade.type)
        assertEquals(currencyPair, trade.currencyPair)
        assertEquals(BigDecimal("0.01293103"), trade.baseAmount)
        assertEquals(BigDecimal("0.01293103") * BigDecimal("2320"), trade.counterAmount)
        assertEquals(BigDecimal("2320"), trade.rate)
    }
}