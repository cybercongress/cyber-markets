package fund.cyber.markets.connectors.poloniex

import com.fasterxml.jackson.databind.JsonNode
import fund.cyber.markets.connectors.common.*
import fund.cyber.markets.model.TradeType.BUY
import fund.cyber.markets.model.TradeType.SELL
import fund.cyber.markets.connectors.common.ws.SaveExchangeMessageParser
import fund.cyber.markets.model.*
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
        channelIdForTokensPairs: Map<String, TokensPair>
): PoloniexMessageParser(channelIdForTokensPairs) {

    override fun parseMessage(jsonNode: JsonNode, tokensPair: TokensPair): ExchangeMessage? {
        val trades = ArrayList<Trade>()

        jsonNode.toList().forEach { node ->
            when(node[0].asText()) {
                "t" -> trades.add(parseTrade(node, tokensPair))
            }
        }

        return TradesUpdatesMessage(trades)
    }
}

class PoloniexOrdersMessageParser(
        channelIdForTokensPairs: Map<String, TokensPair>
): PoloniexMessageParser(channelIdForTokensPairs) {

    override fun parseMessage(jsonNode: JsonNode, tokensPair: TokensPair): ExchangeMessage? {
        val orders = ArrayList<Order>()

        jsonNode.toList().forEach { node ->
            when(node[0].asText()) {
                "o" -> orders.add(parseOrder(node, tokensPair))
                "i" -> return parseOrderBook(node, tokensPair)
            }
        }

        return OrdersUpdatesMessage(type = OrdersUpdateType.COMMON, orders = orders)
    }
}

abstract class PoloniexMessageParser(
        private val channelIdForTokensPairs: Map<String, TokensPair>
) : SaveExchangeMessageParser() {

    override fun parseMessage(jsonRoot: JsonNode): ExchangeMessage? {
        val channelId = jsonRoot.get(0).asText()
        val tokensPair = channelIdForTokensPairs[channelId]
                ?: return ContainingUnknownTokensPairMessage(channelId.toString())


        jsonRoot[2]
        return parseMessage(jsonRoot[2], tokensPair)
    }

    protected abstract fun parseMessage(jsonNode: JsonNode, tokensPair: TokensPair): ExchangeMessage?

    //["t","126320",1,"0.00003328","399377.76875000",1499708547]
    //["t", id, sell/buy,  rate,      quantity,        time(s) ]
    protected fun parseTrade(node: JsonNode, tokensPair: TokensPair): Trade {
        val spotPrice = BigDecimal(node[3].asText())
        val baseAmount = BigDecimal(node[4].asText())
        return Trade (
                tradeId = node[1].asText(), exchange = "Poloniex",
                baseToken = tokensPair.base, quoteToken = tokensPair.quote,
                type = if (node[2].asInt() == 0) SELL else BUY,
                baseAmount = baseAmount, quoteAmount = spotPrice * baseAmount,
                spotPrice = spotPrice, timestamp = node[5].asLong()
        )
    }

    // If amount is 0.00000000 then delete order from book else add or update
    // ["o",0,"0.00003328","0.00000000"]
    protected fun parseOrder(node: JsonNode, tokensPair: TokensPair): Order {
        val spotPrice = BigDecimal(node[2].asText())
        val amount = BigDecimal(node[3].asText())
        return Order (
                type = if (node[1].asInt() == 0) OrderType.SELL else OrderType.BUY,
                exchange = "Poloniex",
                baseToken = tokensPair.base,
                quoteToken = tokensPair.quote,
                spotPrice = spotPrice,
                amount = amount
        )
    }

    protected fun parseOrderBook(node: JsonNode, tokensPair: TokensPair): OrdersUpdatesMessage {
        val orders = mutableListOf<Order>()
        node[1]["orderBook"][0].fields().forEach { entry ->
            orders.add(
                    Order (
                            type = OrderType.SELL,
                            exchange = "Poloniex",
                            baseToken =  tokensPair.base,
                            quoteToken = tokensPair.quote,
                            spotPrice = BigDecimal(entry.key),
                            amount = BigDecimal(entry.value.asText())
                    )
            )
        }
        node[1]["orderBook"][1].fields().forEach { entry ->
            orders.add(
                    Order (
                            type = OrderType.BUY,
                            exchange = "Poloniex",
                            baseToken =  tokensPair.base,
                            quoteToken = tokensPair.quote,
                            spotPrice = BigDecimal(entry.key),
                            amount = BigDecimal(entry.value.asText())
                    )
            )
        }
        return OrdersUpdatesMessage(type = OrdersUpdateType.FULL_ORDER_BOOK, orders = orders)
    }
}