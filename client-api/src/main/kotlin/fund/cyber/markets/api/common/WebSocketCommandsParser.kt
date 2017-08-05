package fund.cyber.markets.api.common

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import fund.cyber.markets.api.configuration.AppContext
import fund.cyber.markets.model.TokensPair


sealed class WebSocketCommand
class UnknownMessage(val message: String) : WebSocketCommand()

// {"subscribe":"trades","pairs":["BTC_ETH","ETH_USD"]}
class TradeChannelSubscriptionCommand(val pairs: List<TokensPair>) : WebSocketCommand()


class WebSocketCommandsParser(
        private val jsonDeserializer: ObjectMapper = AppContext.jsonDeserializer
) {

    fun parseMessage(message: String): WebSocketCommand {
        try {
            val jsonRoot = jsonDeserializer.readTree(message)
            return parseMessage(message, jsonRoot)
        } catch (exception: Exception) {
            return UnknownMessage(message)
        }
    }

    private fun parseMessage(message: String, jsonMessage: JsonNode): WebSocketCommand {
        val topic = jsonMessage["subscribe"].asText()
        return when (topic) {
            "trades" -> parseTradesSubscription(jsonMessage)
            else -> UnknownMessage(message)
        }
    }

    private fun parseTradesSubscription(jsonMessage: JsonNode): WebSocketCommand {
        val pairs = jsonMessage["pairs"].toList()
                .map { pairLabel -> TokensPair.fromLabel(pairLabel.asText(), "_") }
                .toList()
        return TradeChannelSubscriptionCommand(pairs)
    }
}