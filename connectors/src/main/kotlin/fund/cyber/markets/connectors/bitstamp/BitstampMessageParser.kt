package fund.cyber.markets.connectors.bitstamp

import com.fasterxml.jackson.databind.JsonNode
import fund.cyber.markets.connectors.common.*
import fund.cyber.markets.connectors.common.ws.SaveExchangeMessageParser
import fund.cyber.markets.connectors.common.ws.pusher.getPusherEventByValue
import fund.cyber.markets.connectors.jsonParser
import fund.cyber.markets.model.*
import java.math.BigDecimal

private val event_type_trade = "trade"
private val event_type_orders = "data"

class BitstampTradesMessageParser(
        channelIdForTokensPairs: Map<String, TokensPair>
) : BitstampMessageParser(channelIdForTokensPairs) {

    override fun parseMessage(jsonDataString: String, eventType: String, tokensPair: TokensPair): ExchangeMessage? {
        return when (eventType) {
            event_type_trade -> TradesUpdatesMessage(
                    trades = listOf(parseTrade(jsonParser.readTree(jsonDataString), tokensPair))
            )
            else -> null
        }
    }

    private fun parseTrade(tradeNode: JsonNode, tokensPair: TokensPair): Trade {
        val rate = BigDecimal(tradeNode["price"].asText())
        val baseAmount = BigDecimal(tradeNode["amount"].asText())
        val tradeType = if (tradeNode["type"].asInt() == 0) TradeType.BUY else TradeType.SELL
        return Trade.of(
                tradeId = tradeNode["id"].asText(),
                exchange = Exchanges.bitstamp,
                timestamp = tradeNode["timestamp"].asLong() / 1000,
                type = tradeType,
                baseAmount = baseAmount,
                quoteAmount = rate * baseAmount,
                spotPrice = rate,
                tokensPair = tokensPair
        )
    }
}


class BitstampOrdersMessageParser(
        channelIdForTokensPairs: Map<String, TokensPair>
) : BitstampMessageParser(channelIdForTokensPairs) {

    // {"timestamp": "1503225757", "bids": [["0.01084003", "54.57032311"], ["0.01084001", "0"]], "asks": [["0.01088822", "230.92310228"], ["0.01088825", "0"], ["0.01110160", "0"]]}
    override fun parseMessage(jsonDataString: String, eventType: String, tokensPair: TokensPair): ExchangeMessage? {
        return when (eventType) {
            event_type_orders -> OrdersUpdatesMessage(
                    type = OrdersUpdateType.COMMON, exchange = Exchanges.bitstamp,
                    baseToken = tokensPair.base, quoteToken = tokensPair.quote,
                    orders = parseOrders(jsonParser.readTree(jsonDataString), tokensPair)
            )
            else -> null
        }
    }


    private fun parseOrders(orderNode: JsonNode, tokensPair: TokensPair): List<Order> {
        val orders = mutableListOf<Order>()
        orderNode["bids"].forEach { entry ->
            orders.add(
                    Order(
                            type = OrderType.SELL,
                            exchange = Exchanges.bitstamp,
                            baseToken = tokensPair.base,
                            quoteToken = tokensPair.quote,
                            spotPrice = BigDecimal(entry[0].asText()),
                            amount = BigDecimal(entry[1].asText())
                    )
            )
        }
        orderNode["asks"].forEach { entry ->
            orders.add(
                    Order(
                            type = OrderType.BUY,
                            exchange = Exchanges.bitstamp,
                            baseToken = tokensPair.base,
                            quoteToken = tokensPair.quote,
                            spotPrice = BigDecimal(entry[0].asText()),
                            amount = BigDecimal(entry[1].asText())
                    )
            )
        }

        return orders
    }
}

abstract class BitstampMessageParser(
        private val channelIdForTokensPairs: Map<String, TokensPair>
): SaveExchangeMessageParser() {

    override fun parseMessage(jsonRoot: JsonNode): ExchangeMessage? {
        val eventType = jsonRoot["event"].asText()

        val pusherEvent = getPusherEventByValue(eventType)
        if (pusherEvent != null) {
            return pusherEvent.parseMessage(jsonRoot)
        }

        val channelId = jsonRoot["channel"].asText()
        val pairName = channelId.substringAfterLast("_")
        val tokensPair = channelIdForTokensPairs[pairName] ?: return ContainingUnknownTokensPairMessage(channelId)

        return parseMessage(jsonRoot["data"].asText(), eventType, tokensPair)
    }

    protected abstract fun parseMessage(jsonDataString: String, eventType: String, tokensPair: TokensPair) : ExchangeMessage?

}
