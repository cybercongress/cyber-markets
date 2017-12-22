package fund.cyber.markets.api.common

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import fund.cyber.markets.api.common.IncomingMessageGetTopicType.EXCHANGES
import fund.cyber.markets.api.common.IncomingMessageGetTopicType.PAIRS
import fund.cyber.markets.api.common.IncomingMessageGetTopicType.PAIRS_BY_TOKEN
import fund.cyber.markets.api.common.IncomingMessageSubscribeTopicType.ORDERS
import fund.cyber.markets.api.common.IncomingMessageSubscribeTopicType.TICKERS
import fund.cyber.markets.api.common.IncomingMessageSubscribeTopicType.TRADES
import fund.cyber.markets.api.configuration.AppContext
import fund.cyber.markets.dto.TokensPair
import fund.cyber.markets.model.TokensPairInitializer


sealed class WebSocketCommand
class UnknownCommand(val message: String) : WebSocketCommand()

// {"subscribe":"trades","pairs":["BTC_ETH","ETH_USD"]}
// {"subscribe":"orders","pairs":["BTC_ETH","ETH_USD"]}
class ChannelSubscriptionCommand(
        val type: IncomingMessageSubscribeTopicType,
        val pairs: List<TokensPair>,
        val exchanges: List<String>,
        val windowDurations: List<Long> = emptyList()
) : WebSocketCommand()

class InfoCommand(
        val type: IncomingMessageGetTopicType,
        val tokens: List<String>
) : WebSocketCommand()

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
            return parseGetCommand(getTopic, message, jsonMessage)
        }
        if (subscribeTopic != null) {
            return parseSubscribeCommand(subscribeTopic, jsonMessage, message)
        }
        return UnknownCommand(message)
    }

    private fun parseGetCommand(topic: String, message: String, jsonMessage: JsonNode): WebSocketCommand {
        return when {
            topic.toUpperCase() == PAIRS.name && jsonMessage["tokens"] != null -> {
                InfoCommand(PAIRS_BY_TOKEN, jsonMessage["tokens"].toList().map { el ->
                    el.asText().toString()
                })
            }
            topic.toUpperCase() == PAIRS.name -> InfoCommand(PAIRS, emptyList())
            topic.toUpperCase() == EXCHANGES.name -> InfoCommand(EXCHANGES, emptyList())
            else -> UnknownCommand(message)
        }
    }

    private fun parseSubscribeCommand(topic: String, jsonMessage: JsonNode, message: String): WebSocketCommand {
        return when (topic.toUpperCase()) {
            TRADES.name -> ChannelSubscriptionCommand(TRADES, parsePairs(jsonMessage), parseExchanges(jsonMessage))
            ORDERS.name -> ChannelSubscriptionCommand(ORDERS, parsePairs(jsonMessage), parseExchanges(jsonMessage))
            TICKERS.name -> ChannelSubscriptionCommand(TICKERS, parsePairs(jsonMessage), parseExchanges(jsonMessage), parseWindowDurations(jsonMessage))
            else -> UnknownCommand(message)
        }
    }

    private fun parseExchanges(jsonMessage: JsonNode): List<String> {
        return jsonMessage["exchanges"]?.map { exchange -> exchange.asText() } ?: emptyList()
    }

    private fun parsePairs(jsonMessage: JsonNode): List<TokensPair> {
        return jsonMessage["pairs"]?.map { pairLabel -> TokensPairInitializer.fromLabel(pairLabel.asText(), "_").pair }?.distinct() ?: emptyList()
    }

    private fun parseWindowDurations(jsonMessage: JsonNode): List<Long> {
        return jsonMessage["window_durations"]?.map { windowDuration ->
            windowDuration.asLong()
        } ?: emptyList()
    }
}