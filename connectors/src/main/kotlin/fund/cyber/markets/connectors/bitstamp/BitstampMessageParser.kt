package fund.cyber.markets.connectors.bitstamp

import com.fasterxml.jackson.databind.JsonNode
import fund.cyber.markets.connectors.common.*
import fund.cyber.markets.connectors.common.ws.SaveExchangeMessageParser
import fund.cyber.markets.connectors.common.ws.pusher.getPusherEventByValue
import fund.cyber.markets.connectors.jsonParser
import fund.cyber.markets.model.*
import java.math.BigDecimal

private val event_type_trade = "trade"
private val orderEvents = listOf("order_created", "order_changed", "order_deleted")

open class BitstampMessageParser(
        private val channelIdForTokensPairs: Map<String, TokensPair>
): SaveExchangeMessageParser() {

    override fun parseMessage(jsonRoot: JsonNode): List<ExchangeMessage?> {
        val eventType = jsonRoot["event"].asText()

        val pusherEvent = getPusherEventByValue(eventType)
        if (pusherEvent != null) {
            return listOf(pusherEvent.parseMessage(jsonRoot))
        }

        val channelId = jsonRoot["channel"].asText()
        val pairName = channelId.split("_")[2]
        val tokensPair = channelIdForTokensPairs[pairName] ?: return listOf(ContainingUnknownTokensPairMessage(channelId))

        return when (eventType) {
            event_type_trade -> listOf(TradesUpdatesMessage (
                    trades = listOf(parseTrade(jsonParser.readTree(jsonRoot["data"].asText()), tokensPair))
            ))
            in orderEvents -> listOf(OrdersUpdatesMessage (
                    type = OrdersUpdateType.COMMON,
                    orders = listOf(parseOrder(jsonParser.readTree(jsonRoot["data"].asText()), tokensPair))
            ))
            else -> listOf(null)
        }
    }

    fun parseTrade(tradeNode: JsonNode, tokensPair: TokensPair): Trade {
        val rate = BigDecimal(tradeNode["price"].asText())
        val baseAmount = BigDecimal(tradeNode["amount"].asText())
        val tradeType = if (tradeNode["type"].asInt() == 0) TradeType.BUY else TradeType.SELL
        return Trade(
                tradeId = tradeNode["id"].asText(), exchange = "Bitstamp",
                baseToken = tokensPair.base, quoteToken = tokensPair.quote,
                type = tradeType, timestamp = tradeNode["timestamp"].asLong() / 1000,
                baseAmount = baseAmount, quoteAmount = rate * baseAmount, spotPrice = rate
        )
    }

    fun parseOrder(orderNode: JsonNode, tokensPair: TokensPair): Order {
        val spotPrice = BigDecimal(orderNode["price"].asText())
        val amount = BigDecimal(orderNode["amount"].asText())
        val orderType = if (orderNode["order_type"].asInt() == 0) OrderType.BUY else OrderType.SELL
        return Order(
                exchange = "Bitstamp",
                baseToken = tokensPair.base,
                quoteToken = tokensPair.quote,
                spotPrice = spotPrice,
                amount = amount,
                type = orderType
        )
    }
}
