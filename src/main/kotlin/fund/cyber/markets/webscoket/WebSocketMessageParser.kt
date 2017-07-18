package fund.cyber.markets.webscoket

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory


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

    private val LOG = LoggerFactory.getLogger(BasicWebSocketMessageParser::class.java)

    private val jsonReader = ObjectMapper()

    override fun parseMessage(message: String): ExchangeMessage {
        try {
            val jsonRoot = jsonReader.readTree(message)
            return parseMessage(jsonRoot) ?: UnknownFormatMessage(message)
        } catch (exception: Exception) {
            LOG.debug("Exception during parsing message", exception)
            return UnknownFormatMessage(message)
        }
    }

    abstract fun parseMessage(jsonRoot: JsonNode): ExchangeMessage?
}