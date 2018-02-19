package fund.cyber.markets.connectors.common.ws.pusher

import com.fasterxml.jackson.databind.JsonNode
import fund.cyber.markets.connectors.common.ExchangeMessage
import fund.cyber.markets.connectors.jsonParser

/**
 * Created by aalbov on 2.8.17.
 */

fun getPusherEventByValue(value: String): PusherEvent? {
    for (event in PusherEvent.values()) {
        if (value == event.value) return event
    }
    return null
}

enum class PusherEvent(val value: String) {
    ERROR ("pusher:error") {
        override fun parseMessage(jsonRoot: JsonNode): ExchangeMessage {
            val data = jsonParser.readTree(jsonRoot["data"].asText());
            return PusherErrorMessage(data["message"].asText(), data["code"].asInt())
        }
    },
    SUBSCRIPTION_SUCCEEDED ("pusher_internal:subscription_succeeded") {
        override fun parseMessage(jsonRoot: JsonNode): ExchangeMessage {
            return PusherSubscriptionSucceededMessage(jsonRoot["channel"].asText())
        }
    },
    CONNECTION_ESTABLISHED ("pusher:connection_established") {
        override fun parseMessage(jsonRoot: JsonNode): ExchangeMessage {
            val data = jsonParser.readTree(jsonRoot["data"].asText());
            return PusherConnectionEstablishedMessage(data["socket_id"].asDouble())
        }
    };

    abstract fun parseMessage(jsonRoot: JsonNode): ExchangeMessage
}

abstract class PusherMessage (): ExchangeMessage() {
    abstract fun message(exchangeName: String): String
}

class PusherErrorMessage (
        val message: String,
        val code: Int
) : PusherMessage() {
    override fun message(exchangeName: String): String {
        return "ERROR on ${exchangeName} exchange. ${message}. Pusher error code ${code}"
    }
}

class PusherSubscriptionSucceededMessage (
        val channel: String
) : PusherMessage() {
    override fun message(exchangeName: String): String {
        return "Subscribed to ${exchangeName} ${channel} channel"
    }
}

class PusherConnectionEstablishedMessage (
        val socketId: Double
) : PusherMessage() {
    override fun message(exchangeName: String): String {
        return "${exchangeName} pusher connection established. Socket ${socketId}"
    }
}