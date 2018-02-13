package fund.cyber.markets.connectors.gdax.connector

import com.fasterxml.jackson.databind.JsonNode
import fund.cyber.markets.connectors.common.ContainingUnknownTokensPairMessage
import fund.cyber.markets.connectors.common.ExchangeMessage
import fund.cyber.markets.connectors.common.OrdersUpdateType
import fund.cyber.markets.connectors.common.OrdersUpdatesMessage
import fund.cyber.markets.connectors.common.TradesUpdatesMessage
import fund.cyber.markets.connectors.common.ws.SaveExchangeMessageParser
import fund.cyber.markets.helpers.MILLIS_TO_SECONDS
import fund.cyber.markets.helpers.convert
import fund.cyber.markets.model.Exchanges
import fund.cyber.markets.model.Order
import fund.cyber.markets.model.OrderType
import fund.cyber.markets.model.TokensPairInitializer
import fund.cyber.markets.model.Trade
import fund.cyber.markets.model.TradeType
import java.math.BigDecimal
import java.sql.Timestamp
import java.time.ZonedDateTime


/**
 * GDAX exchange ws messages parser.
 *
 * GDAX sends instead of trade baseamount -> number of lots. Each {@link TokensPairInitializer} contains lot size.
 *
 */
class GdaxTradesMessageParser(
        private val channelSymbolForTokensPair: Map<String, TokensPairInitializer>
) : SaveExchangeMessageParser() {

    override fun parseMessage(jsonRoot: JsonNode): ExchangeMessage? {
        return when {
            jsonRoot["type"].asText() == "last_match" -> parseMarketDataIncrementalRefresh(jsonRoot)
            jsonRoot["type"].asText() == "match" -> parseMarketDataIncrementalRefresh(jsonRoot)
            else -> null
        }
    }

    private fun parseMarketDataIncrementalRefresh(node: JsonNode): ExchangeMessage {

        val symbol = node["product_id"].asText()
        val tokensPair =
                channelSymbolForTokensPair[symbol] ?: return ContainingUnknownTokensPairMessage(symbol)
        val baseAmount = BigDecimal(node["size"].asText())
        val spotPrice = BigDecimal(node["price"].asText())
        val trades = listOf(
                Trade.of(
                        tradeId = node["trade_id"].asText(),
                        exchange = Exchanges.gdax,
                        timestamp = Timestamp.valueOf(
                                ZonedDateTime.parse(node["time"].asText()).toLocalDateTime()).time convert MILLIS_TO_SECONDS,
                        type = TradeType.valueOf(node["side"].asText().toUpperCase()),
                        baseAmount = BigDecimal(node["size"].asText()),
                        quoteAmount = spotPrice * baseAmount,
                        spotPrice = spotPrice,
                        tokensPairInitializer = tokensPair
                )
        )
        return TradesUpdatesMessage(trades)
    }

}

class GdaxOrdersMessageParser(
        private val channelSymbolForTokensPair: Map<String, TokensPairInitializer>
) : SaveExchangeMessageParser() {

    override fun parseMessage(jsonRoot: JsonNode): ExchangeMessage? {
        return when {
            jsonRoot["type"]?.asText() == "snapshot" -> parseOrdersSnapshot(jsonRoot)
            jsonRoot["type"]?.asText() == "l2update" -> parseOrdersUpdate(jsonRoot)
            else -> null
        }
    }

    private fun parseOrdersSnapshot(node: JsonNode): ExchangeMessage {
        return parseOrders(node, OrdersUpdateType.FULL_ORDER_BOOK)
    }

    private fun parseOrdersUpdate(node: JsonNode): ExchangeMessage {
        return parseOrders(node, OrdersUpdateType.COMMON)
    }

    private fun parseOrders(node: JsonNode, ordersUpdateType: OrdersUpdateType): ExchangeMessage {
        val symbol = node["product_id"].asText()
        val tokensPair = channelSymbolForTokensPair[symbol] ?: return ContainingUnknownTokensPairMessage(symbol)
        when (ordersUpdateType) {
            OrdersUpdateType.FULL_ORDER_BOOK -> {
                val asks = node["asks"].toList().map {
                    askNode -> parseOrder(askNode, tokensPair, OrderType.BUY, ordersUpdateType)
                }
                val bids = node["bids"].toList().map {
                    bidNode -> parseOrder(bidNode, tokensPair, OrderType.SELL, ordersUpdateType)
                }

                return OrdersUpdatesMessage(
                        type = ordersUpdateType,
                        exchange = Exchanges.gdax,
                        baseToken = tokensPair.pair.base,
                        quoteToken = tokensPair.pair.quote,
                        orders = listOf(*asks.toTypedArray(), *bids.toTypedArray())
                )
            }
            OrdersUpdateType.COMMON -> {
                val ordersUpdate = node["changes"].toList().map {
                    order -> parseOrder(
                        order, tokensPair, OrderType.valueOf(order[0].asText().toUpperCase()), ordersUpdateType
                    )
                }
                return OrdersUpdatesMessage(
                        type = ordersUpdateType,
                        exchange = Exchanges.gdax,
                        baseToken = tokensPair.pair.base,
                        quoteToken = tokensPair.pair.quote,
                        orders = listOf(*ordersUpdate.toTypedArray())
                )
            }
        }
    }

    private fun parseOrder(askNode: JsonNode, tokensPair: TokensPairInitializer, orderType: OrderType, updateType: OrdersUpdateType): Order {
        when (updateType) {
            OrdersUpdateType.COMMON -> {
                val amount = BigDecimal(askNode[2].asText())
                val spotPrice = BigDecimal(askNode[1].asText())
                return Order(
                        type = orderType,
                        exchange = Exchanges.gdax,
                        baseToken = tokensPair.pair.base,
                        quoteToken = tokensPair.pair.quote,
                        spotPrice = spotPrice,
                        amount = amount
                )
            }
            OrdersUpdateType.FULL_ORDER_BOOK -> {
                val amount = BigDecimal(askNode[1].asText())
                val spotPrice = BigDecimal(askNode[0].asText())
                return Order(
                        type = orderType,
                        exchange = Exchanges.gdax,
                        baseToken = tokensPair.pair.base,
                        quoteToken = tokensPair.pair.quote,
                        spotPrice = spotPrice,
                        amount = amount
                )
            }
        }
    }
}