package fund.cyber.markets.api.common

import com.fasterxml.jackson.databind.JsonNode
import fund.cyber.markets.jsonParser
import fund.cyber.markets.model.TokensPair


sealed class WebSocketCommand
class UnknownMessage(val message: String) : WebSocketCommand()

// {"subscribe":"trades","pairs":["BTC_ETH","ETH_USD"]}
class TradeChannelSubscribtionCommand(val pairs: List<TokensPair>) : WebSocketCommand()


class WebSocketCommandsParser {

    fun parseMessage(message: String): WebSocketCommand {
        try {
            val jsonRoot = jsonParser.readTree(message)
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
        return TradeChannelSubscribtionCommand(pairs)
    }
}