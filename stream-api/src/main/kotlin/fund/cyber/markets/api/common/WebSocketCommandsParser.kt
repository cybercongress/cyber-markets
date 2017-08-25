package fund.cyber.markets.api.common

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import fund.cyber.markets.api.configuration.AppContext
import fund.cyber.markets.model.TokensPair


sealed class WebSocketCommand
class UnknownCommand(val message: String) : WebSocketCommand()

// {"subscribe":"trades","pairs":["BTC_ETH","ETH_USD"]}
class TradeChannelSubscriptionCommand(val pairs: List<TokensPair>?,
                                      val exchanges: List<String>?) : WebSocketCommand()

class TradeChannelInfoCommand(val type: IncomingMessageGetTopicType) : WebSocketCommand()


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
        if (getTopic!=null) {
            return when (getTopic.toUpperCase()) {
                IncomingMessageGetTopicType.PAIRS.name ->
                    TradeChannelInfoCommand(IncomingMessageGetTopicType.PAIRS)
                IncomingMessageGetTopicType.EXCHANGES.name ->
                    TradeChannelInfoCommand(IncomingMessageGetTopicType.EXCHANGES)
                else -> UnknownCommand(message)
            }
        }
        return when (subscribeTopic) {
            "trades" -> parseTradesSubscription(jsonMessage)
            else -> UnknownCommand(message)
        }
    }

    private fun parseTradesSubscription(jsonMessage: JsonNode): WebSocketCommand {
        var pairs = jsonMessage["pairs"]?.toList()
                ?.map { pairLabel -> TokensPair.fromLabel(pairLabel.asText(), "_") }
                ?.toList()
        var exchanges = jsonMessage["exchanges"]?.toList()
                ?.map { exchange -> exchange.asText() }
                ?.toList()
        return TradeChannelSubscriptionCommand(pairs, exchanges);
    }
}