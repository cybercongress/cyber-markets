package fund.cyber.markets.connectors.bitstamp.connector

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
        channelIdForTokensPairsInitializer: Map<String, TokensPairInitializer>
) : BitstampMessageParser(channelIdForTokensPairsInitializer) {

    override fun parseMessage(jsonDataString: String, eventType: String, tokensPairInitializer: TokensPairInitializer): ExchangeMessage? {
        return when (eventType) {
            event_type_trade -> TradesUpdatesMessage(
                    trades = listOf(parseTrade(jsonParser.readTree(jsonDataString), tokensPairInitializer))
            )
            else -> null
        }
    }

    private fun parseTrade(tradeNode: JsonNode, tokensPairInitializer: TokensPairInitializer): Trade {
        val rate = BigDecimal(tradeNode["price"].asText())
        val baseAmount = BigDecimal(tradeNode["amount"].asText())
        val tradeType = if (tradeNode["type"].asInt() == 0) TradeType.BUY else TradeType.SELL
        return Trade.of(
                tradeId = tradeNode["id"].asText(),
                exchange = Exchanges.bitstamp,
                timestamp = tradeNode["timestamp"].asLong(),
                type = tradeType,
                baseAmount = baseAmount,
                quoteAmount = rate * baseAmount,
                spotPrice = rate,
                tokensPairInitializer = tokensPairInitializer
        )
    }
}


class BitstampOrdersMessageParser(
        channelIdForTokensPairsInitializer: Map<String, TokensPairInitializer>
) : BitstampMessageParser(channelIdForTokensPairsInitializer) {

    // {"timestamp": "1503225757", "bids": [["0.01084003", "54.57032311"], ["0.01084001", "0"]], "asks": [["0.01088822", "230.92310228"], ["0.01088825", "0"], ["0.01110160", "0"]]}
    override fun parseMessage(jsonDataString: String, eventType: String, tokensPairInitializer: TokensPairInitializer): ExchangeMessage? {
        return when (eventType) {
            event_type_orders -> OrdersUpdatesMessage(
                    type = OrdersUpdateType.COMMON, exchange = Exchanges.bitstamp,
                    baseToken = tokensPairInitializer.pair.base, quoteToken = tokensPairInitializer.pair.quote,
                    orders = parseOrders(jsonParser.readTree(jsonDataString), tokensPairInitializer)
            )
            else -> null
        }
    }


    private fun parseOrders(orderNode: JsonNode, tokensPairInitializer: TokensPairInitializer): List<Order> {
        val orders = mutableListOf<Order>()
        orderNode["bids"].forEach { entry ->
            orders.add(
                    Order(
                            type = OrderType.SELL,
                            exchange = Exchanges.bitstamp,
                            baseToken = tokensPairInitializer.pair.base,
                            quoteToken = tokensPairInitializer.pair.quote,
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
                            baseToken = tokensPairInitializer.pair.base,
                            quoteToken = tokensPairInitializer.pair.quote,
                            spotPrice = BigDecimal(entry[0].asText()),
                            amount = BigDecimal(entry[1].asText())
                    )
            )
        }

        return orders
    }
}

abstract class BitstampMessageParser(
        private val channelIdForTokensPairsInitializer: Map<String, TokensPairInitializer>
): SaveExchangeMessageParser() {

    override fun parseMessage(jsonRoot: JsonNode): ExchangeMessage? {
        val eventType = jsonRoot["event"].asText()

        val pusherEvent = getPusherEventByValue(eventType)
        if (pusherEvent != null) {
            return pusherEvent.parseMessage(jsonRoot)
        }

        val channelId = jsonRoot["channel"].asText()
        val pairName = channelId.substringAfterLast("_")
        val tokensPair = channelIdForTokensPairsInitializer[pairName] ?: return ContainingUnknownTokensPairMessage(channelId)

        return parseMessage(jsonRoot["data"].asText(), eventType, tokensPair)
    }

    protected abstract fun parseMessage(jsonDataString: String, eventType: String, tokensPairInitializer: TokensPairInitializer) : ExchangeMessage?

}
