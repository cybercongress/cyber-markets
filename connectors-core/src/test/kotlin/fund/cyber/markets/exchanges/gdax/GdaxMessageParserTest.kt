package fund.cyber.markets.exchanges.gdax

import fund.cyber.markets.connectors.common.ContainingUnknownTokensPairMessage
import fund.cyber.markets.connectors.common.TradesUpdatesMessage
import fund.cyber.markets.connectors.gdax.connector.GdaxTradesMessageParser
import fund.cyber.markets.model.TokensPairInitializer
import fund.cyber.markets.model.Trade
import fund.cyber.markets.model.TradeType.SELL
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.math.BigDecimal

@DisplayName("GDAX Message Parser: ")
class GdaxMessageParserTest {

    @Test
    @DisplayName("Should parse message into right trade")
    fun testParseTrade() {

        val message = """
            {"type":"match",
            "trade_id":5913120,
            "maker_order_id":"713c976c-be1e-4afc-90e8-dac22a6bbf91",
            "taker_order_id":"4fc107e1-9ca8-47f5-b5d2-72a8000669e9",
            "side":"sell",
            "size":"0.01820935",
            "price":"8655.22000000",
            "product_id":"BTC-EUR",
            "sequence":2979154027,
            "time":"2017-07-13T21:56:39.567000Z"}
            """.trimIndent()
        val tokensPair = TokensPairInitializer("BTC", "EUR")
        val tradesChannelSymbolForTokensPair = mapOf("BTC-EUR" to tokensPair)
        val messageParser = GdaxTradesMessageParser(tradesChannelSymbolForTokensPair)

        val exchangeMessage = messageParser.parseMessage(message)
        assertTrue(exchangeMessage is TradesUpdatesMessage)
        assertTrue((exchangeMessage as TradesUpdatesMessage).trades.size == 1)

        val trade = Trade.of(
                tradeId = "5913120",
                exchange = "GDAX",
                timestamp = 1499972199,
                type = SELL,
                baseAmount = BigDecimal("0.01820935"),
                quoteAmount = BigDecimal("157.6059303070000000"),
                spotPrice = BigDecimal("8655.22000000"),
                tokensPairInitializer = tokensPair
        )
        assertEquals(trade, exchangeMessage.trades[0])
    }

    @Test
    @DisplayName("Should not parse due to containing unknown tokens pair")
    fun testParseMessageWithUnknownTokensPair() {

        val message = """
            {"type":"match",
            "trade_id":5913120,
            "maker_order_id":"713c976c-be1e-4afc-90e8-dac22a6bbf91",
            "taker_order_id":"4fc107e1-9ca8-47f5-b5d2-72a8000669e9",
            "side":"sell",
            "size":"0.01820935",
            "price":"8655.22000000",
            "product_id":"BTC-EUR",
            "sequence":2979154027,
            "time":"2017-07-13T21:56:39.567000Z"}
            """.trimIndent()
        val messageParser = GdaxTradesMessageParser(emptyMap())

        val exchangeMessage = messageParser.parseMessage(message)
        Assertions.assertTrue(exchangeMessage is ContainingUnknownTokensPairMessage)
        Assertions.assertEquals("BTC-EUR", (exchangeMessage as ContainingUnknownTokensPairMessage).symbol)
    }
}