package fund.cyber.markets.exchanges.common

import com.fasterxml.jackson.databind.JsonNode
import fund.cyber.markets.webscoket.ExchangeMessage
import fund.cyber.markets.webscoket.SaveExchangeMessageParser
import fund.cyber.markets.webscoket.UnknownFormatMessage
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test


@DisplayName("Every message parser:")
class BasicWebSocketMessageParserTest {

    private class NoOpBasicWebSocketMessageParser : SaveExchangeMessageParser() {
        override fun parseMessage(jsonRoot: JsonNode): ExchangeMessage? {
            return null
        }
    }

    private val messageParser = NoOpBasicWebSocketMessageParser()

    @Test
    @DisplayName("Should not throw exception for invalid provided json, but return UnknownFormatMessage")
    fun testInvalidJsonProvided() {
        val message = "[q34342%&$&__~~~~"
        val exchangeMessage = messageParser.parseMessage(message)

        Assertions.assertTrue(exchangeMessage is UnknownFormatMessage)
        exchangeMessage as UnknownFormatMessage
        Assertions.assertEquals(message, exchangeMessage.message)
    }

    @Test
    @DisplayName("Should not throw exception for unknown json structure, but return UnknownFormatMessage")
    fun testUnknownJsonStructureProvided() {
        val message = """{"event":"shutdown",[53]}"""
        val exchangeMessage = messageParser.parseMessage(message)

        Assertions.assertTrue(exchangeMessage is UnknownFormatMessage)
        exchangeMessage as UnknownFormatMessage
        Assertions.assertEquals(message, exchangeMessage.message)
    }
}