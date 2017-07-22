package fund.cyber.markets.webscoket

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import fund.cyber.markets.helpers.logger


/**
 * Parse exchange messages.
 */
interface WebSocketMessageParser {
    /**
     * Parse message obtained from exchange.
     * Contract:
     * -For invalid json returns [UnknownFormatMessage].
     * -For unknown message returns [UnknownFormatMessage].
     * -For message contains unknown tokens pair returns [ContainingUnknownTokensPairMessage]
     */
    fun parseMessage(message: String): ExchangeMessage
}

abstract class BasicWebSocketMessageParser(
        val exchange: String
) : WebSocketMessageParser {

    private val jsonReader = ObjectMapper()

    override fun parseMessage(message: String): ExchangeMessage {
        try {
            val jsonRoot = jsonReader.readTree(message)
            return parseMessage(jsonRoot) ?: UnknownFormatMessage(message)
        } catch (exception: Exception) {
            LOGGER.debug("Exception during parsing message", exception)
            return UnknownFormatMessage(message)
        }
    }

    abstract fun parseMessage(jsonRoot: JsonNode): ExchangeMessage?

    companion object {
        private val LOGGER = logger(BasicWebSocketMessageParser::class)
    }
}