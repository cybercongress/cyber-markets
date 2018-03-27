package fund.cyber.markets.connectors.poloniex.connector

import com.fasterxml.jackson.databind.JsonNode
import fund.cyber.markets.connectors.common.ContainingUnknownTokensPairMessage
import fund.cyber.markets.connectors.common.ExchangeMessage
import fund.cyber.markets.connectors.common.OrdersUpdateType
import fund.cyber.markets.connectors.common.OrdersUpdatesMessage
import fund.cyber.markets.connectors.common.TradesUpdatesMessage
import fund.cyber.markets.connectors.common.ws.SaveExchangeMessageParser
import fund.cyber.markets.model.Exchanges
import fund.cyber.markets.model.Order
import fund.cyber.markets.model.OrderType
import fund.cyber.markets.model.TokensPairInitializer
import fund.cyber.markets.model.Trade
import fund.cyber.markets.model.TradeType.BUY
import fund.cyber.markets.model.TradeType.SELL
import java.math.BigDecimal

/**
 *   Parses poloniex ws message.
 *
 *
 *   0-1000 channels Message template [ channelId, seq number, [data array]]
 *   data array contains next items: orders, trades, order book
 *
 *   0-1000 channels Message example
 *  [ 129, 4679255, [["o",0,"0.00003328","0.00000000"], ["t","126320",1,"0.00003328","399377.76875000",1499708547]] ]
 *
 *  @author hleb.albau@gmail.com
 */
class PoloniexTradesMessageParser(
        channelIdForTokensPairsInitializer: Map<String, TokensPairInitializer>
): PoloniexMessageParser(channelIdForTokensPairsInitializer) {

    override fun parseMessage(jsonNode: JsonNode, tokensPairInitializer: TokensPairInitializer): ExchangeMessage? {
        val trades = ArrayList<Trade>()

        jsonNode.toList().forEach { node ->
            when(node[0].asText()) {
                "t" -> trades.add(parseTrade(node, tokensPairInitializer))
            }
        }

        return TradesUpdatesMessage(trades)
    }

    //["t","126320",1,"0.00003328","399377.76875000",1499708547]
    //["t", id, sell/buy,  rate,      quantity,        time(s) ]
    private fun parseTrade(node: JsonNode, tokensPairInitializer: TokensPairInitializer): Trade {
        val spotPrice = BigDecimal(node[3].asText())
        val baseAmount = BigDecimal(node[4].asText())
        val quoteAmount = spotPrice * baseAmount
        val type = if (node[2].asInt() == 0) SELL else BUY
        return Trade.of(
                tradeId = node[1].asText(),
                exchange = Exchanges.poloniex,
                timestamp = node[5].asLong(),
                type = type,
                baseAmount = baseAmount,
                quoteAmount = quoteAmount,
                spotPrice = spotPrice,
                tokensPairInitializer = tokensPairInitializer
        )
    }
}

class PoloniexOrdersMessageParser(
        channelIdForTokensPairsInitializer: Map<String, TokensPairInitializer>
): PoloniexMessageParser(channelIdForTokensPairsInitializer) {

    override fun parseMessage(jsonNode: JsonNode, tokensPairInitializer: TokensPairInitializer): ExchangeMessage? {
        val orders = ArrayList<Order>()

        jsonNode.toList().forEach { node ->
            when(node[0].asText()) {
                "o" -> orders.add(parseOrder(node, tokensPairInitializer))
                "i" -> return parseOrderBook(node, tokensPairInitializer)
            }
        }

        return OrdersUpdatesMessage(type = OrdersUpdateType.COMMON, exchange = Exchanges.poloniex,
                baseToken = tokensPairInitializer.pair.base, quoteToken = tokensPairInitializer.pair.quote, orders = orders)
    }

    // If amount is 0.00000000 then delete order from book else add or update
    // ["o",0,"0.00003328","0.00000000"]
    private fun parseOrder(node: JsonNode, tokensPairInitializer: TokensPairInitializer): Order {
        val spotPrice = BigDecimal(node[2].asText())
        val amount = BigDecimal(node[3].asText())
        return Order (
                type = if (node[1].asInt() == 0) OrderType.SELL else OrderType.BUY,
                exchange = Exchanges.poloniex,
                baseToken = tokensPairInitializer.pair.base,
                quoteToken = tokensPairInitializer.pair.quote,
                spotPrice = spotPrice,
                amount = amount
        )
    }

    private fun parseOrderBook(node: JsonNode, tokensPairInitializer: TokensPairInitializer): OrdersUpdatesMessage {
        val orders = mutableListOf<Order>()
        node[1]["orderBook"][0].fields().forEach { entry ->
            orders.add(
                    Order (
                            type = OrderType.SELL,
                            exchange = Exchanges.poloniex,
                            baseToken =  tokensPairInitializer.pair.base,
                            quoteToken = tokensPairInitializer.pair.quote,
                            spotPrice = BigDecimal(entry.key),
                            amount = BigDecimal(entry.value.asText())
                    )
            )
        }
        node[1]["orderBook"][1].fields().forEach { entry ->
            orders.add(
                    Order (
                            type = OrderType.BUY,
                            exchange = Exchanges.poloniex,
                            baseToken =  tokensPairInitializer.pair.base,
                            quoteToken = tokensPairInitializer.pair.quote,
                            spotPrice = BigDecimal(entry.key),
                            amount = BigDecimal(entry.value.asText())
                    )
            )
        }
        return OrdersUpdatesMessage(type = OrdersUpdateType.FULL_ORDER_BOOK, exchange = Exchanges.poloniex,
                baseToken = tokensPairInitializer.pair.base, quoteToken = tokensPairInitializer.pair.quote, orders = orders)
    }
}

abstract class PoloniexMessageParser(
        private val channelIdForTokensPairsInitializer: Map<String, TokensPairInitializer>
) : SaveExchangeMessageParser() {

    override fun parseMessage(jsonRoot: JsonNode): ExchangeMessage? {
        val channelId = jsonRoot.get(0).asText()
        val tokensPair = channelIdForTokensPairsInitializer[channelId]
                ?: return ContainingUnknownTokensPairMessage(channelId.toString())


        jsonRoot[2]
        return parseMessage(jsonRoot[2], tokensPair)
    }

    protected abstract fun parseMessage(jsonNode: JsonNode, tokensPairInitializer: TokensPairInitializer): ExchangeMessage?
}