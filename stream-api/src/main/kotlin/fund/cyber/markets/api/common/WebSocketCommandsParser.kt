package fund.cyber.markets.api.common

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import fund.cyber.markets.api.common.IncomingMessageGetTopicType.*
import fund.cyber.markets.api.common.IncomingMessageSubscribeTopicType.*
import fund.cyber.markets.api.configuration.AppContext
import fund.cyber.markets.model.TokensPairInitializer


sealed class WebSocketCommand
class UnknownCommand(val message: String) : WebSocketCommand()

// {"subscribe":"trades","pairs":["BTC_ETH","ETH_USD"]}
// {"subscribe":"orders","pairs":["BTC_ETH","ETH_USD"]}
class ChannelSubscriptionCommand(
        val type: IncomingMessageSubscribeTopicType,
        val pairs: List<TokensPairInitializer>,
        val exchanges: List<String>
) : WebSocketCommand()

class InfoCommand(val type: IncomingMessageGetTopicType) : WebSocketCommand()

class WebSocketCommandsParser(
        private val jsonDeserializer: ObjectMapper = AppContext.jsonDeserializer
) {

    fun parseMessage(message: String): WebSocketCommand {
        try {
            val jsonRoot = jsonDeserializer.readTree(message)
            return parseMessage(message, jsonRoot)
        } catch (exception: Exception) {
            return UnknownCommand(message)
        }
    }

    private fun parseMessage(message: String, jsonMessage: JsonNode): WebSocketCommand {
        val subscribeTopic = jsonMessage["subscribe"]?.asText()
        val getTopic = jsonMessage["get"]?.asText()
        if (getTopic != null) {
            return parseGetCommand(getTopic, message)
        }
        if (subscribeTopic != null) {
            return parseSubscribeCommand(subscribeTopic, jsonMessage, message)
        }
        return UnknownCommand(message)
    }

    private fun parseGetCommand(topic: String, message: String): WebSocketCommand {
        return when (topic.toUpperCase()) {
            PAIRS.name -> InfoCommand(PAIRS)
            EXCHANGES.name -> InfoCommand(EXCHANGES)
            else -> UnknownCommand(message)
        }
    }

    private fun parseSubscribeCommand(topic: String, jsonMessage: JsonNode, message: String): WebSocketCommand {
        return when (topic.toUpperCase()) {
            TRADES.name -> ChannelSubscriptionCommand(TRADES, parsePairs(jsonMessage), parseExchanges(jsonMessage))
            ORDERS.name -> ChannelSubscriptionCommand(ORDERS, parsePairs(jsonMessage), parseExchanges(jsonMessage))
            else -> UnknownCommand(message)
        }
    }

    private fun parseExchanges(jsonMessage: JsonNode): List<String> {
        return jsonMessage["exchanges"]?.map { exchange -> exchange.asText() } ?: emptyList()
    }

    private fun parsePairs(jsonMessage: JsonNode): List<TokensPairInitializer> {
        return jsonMessage["pairs"]?.map {
            pairLabel -> TokensPairInitializer.fromLabel(pairLabel.asText(), "_") }?.distinct() ?: emptyList()
    }
}