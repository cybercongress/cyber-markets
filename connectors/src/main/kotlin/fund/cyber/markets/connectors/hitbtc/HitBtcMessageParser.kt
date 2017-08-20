package fund.cyber.markets.connectors.hitbtc

import com.fasterxml.jackson.databind.JsonNode
import fund.cyber.markets.connectors.common.*
import fund.cyber.markets.model.Trade
import fund.cyber.markets.model.TradeType
import fund.cyber.markets.connectors.common.ws.SaveExchangeMessageParser
import fund.cyber.markets.model.Order
import fund.cyber.markets.model.OrderType
import java.math.BigDecimal


/**
 * HitBtc exchange ws messages parser.
 *
 * HitBtc sends instead of trade baseamount -> number of lots. Each {@link HitBtcTokensPair} contains lot size.
 *
 */
class HitBtcTradesMessageParser(
        private val channelSymbolForTokensPair: Map<String, HitBtcTokensPair>
) : SaveExchangeMessageParser() {

    override fun parseMessage(jsonRoot: JsonNode): ExchangeMessage? {
        val marketDataIncrementalRefreshNode = jsonRoot["MarketDataIncrementalRefresh"] ?: return null
        return parseMarketDataIncrementalRefresh(marketDataIncrementalRefreshNode)
    }

    private fun parseMarketDataIncrementalRefresh(node: JsonNode): ExchangeMessage {

        val symbol = node["symbol"].asText()
        val tokensPair = channelSymbolForTokensPair[symbol] ?: return ContainingUnknownTokensPairMessage(symbol)

        val timestamp = node["timestamp"].asLong() / 1000

        val trades = node["trade"].toList()
                .map { tradeNode ->
                    val baseAmount = BigDecimal(tradeNode["size"].asText()).multiply(tokensPair.lotSize)
                    val spotPrice = BigDecimal(tradeNode["price"].asText())
                    Trade(
                            tradeId = tradeNode["tradeId"].asText(), exchange = "HitBtc",
                            baseToken = tokensPair.base, quoteToken = tokensPair.quote,
                            type = TradeType.valueOf(tradeNode["side"].asText().toUpperCase()),
                            baseAmount = baseAmount, quoteAmount = spotPrice * baseAmount,
                            spotPrice = spotPrice, timestamp = timestamp
                    )
                }
        return TradesUpdatesMessage(trades)
    }

}

class HitBtcOrdersMessageParser(
        private val channelSymbolForTokensPair: Map<String, HitBtcTokensPair>
) : SaveExchangeMessageParser() {

    override fun parseMessage(jsonRoot: JsonNode): ExchangeMessage? {
        if (jsonRoot["MarketDataSnapshotFullRefresh"] != null) {
            return parseMarketDataSnapshotFullRefresh(jsonRoot["MarketDataSnapshotFullRefresh"])
        } else if (jsonRoot["MarketDataIncrementalRefresh"] != null) {
            return parseMarketDataIncrementalRefresh(jsonRoot["MarketDataIncrementalRefresh"])
        } else return null
    }

    private fun parseMarketDataSnapshotFullRefresh(node: JsonNode): ExchangeMessage {
        return parseOrders(node, OrdersUpdateType.FULL_ORDER_BOOK)
    }

    private fun parseMarketDataIncrementalRefresh(node: JsonNode): ExchangeMessage {
        return parseOrders(node, OrdersUpdateType.COMMON)
    }

    private fun parseOrders(node: JsonNode, ordersUpdateType: OrdersUpdateType): ExchangeMessage {
        val symbol = node["symbol"].asText()
        val tokensPair = channelSymbolForTokensPair[symbol] ?: return ContainingUnknownTokensPairMessage(symbol)
        val asks = node["ask"].toList().map { askNode -> parseOrder(askNode, tokensPair, OrderType.BUY) }
        val bids = node["bid"].toList().map { bidNode -> parseOrder(bidNode, tokensPair, OrderType.SELL) }

        return OrdersUpdatesMessage(
                type = ordersUpdateType,
                orders = listOf(*asks.toTypedArray(), *bids.toTypedArray())
        )
    }

    private fun parseOrder(askNode: JsonNode, tokensPair: HitBtcTokensPair, orderType: OrderType): Order {
        val amount = BigDecimal(askNode["size"].asText()).multiply(tokensPair.lotSize)
        val spotPrice = BigDecimal(askNode["price"].asText())
        return Order(
                type = orderType, exchange = "Bitfinex",
                baseToken = tokensPair.base, quoteToken = tokensPair.quote,
                spotPrice = spotPrice, amount = amount
        )
    }
}