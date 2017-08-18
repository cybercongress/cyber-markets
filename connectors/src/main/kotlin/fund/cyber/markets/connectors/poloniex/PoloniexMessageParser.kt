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


open class PoloniexMessageParser(
        private val channelIdForTokensPairs: Map<Int, TokensPair>
) : SaveExchangeMessageParser() {

    override fun parseMessage(jsonRoot: JsonNode): List<ExchangeMessage?> {
        val channelId = jsonRoot.get(0).asInt()
        val tokensPair = channelIdForTokensPairs[channelId]
                ?: return listOf(ContainingUnknownTokensPairMessage(channelId.toString()))

        val trades = ArrayList<Trade>()
        val orders = ArrayList<Order>()

        val orderBookMessages = mutableListOf<OrdersUpdatesMessage>()


        jsonRoot[2].toList().forEach { node ->
            when(node[0].asText()) {
                "t" -> trades.add(parseTrade(node, tokensPair))
                "o" -> orders.add(parseOrder(node, tokensPair))
                "i" -> orderBookMessages.add(parseOrderBook(node, tokensPair))
            }
        }

        return listOf(
                TradesUpdatesMessage(trades),
                OrdersUpdatesMessage(type = OrdersUpdateType.COMMON, orders = orders),
                *orderBookMessages.toTypedArray()
        )
    }

    //["t","126320",1,"0.00003328","399377.76875000",1499708547]
    //["t", id, sell/buy,  rate,      quantity,        time(s) ]
    private fun parseTrade(node: JsonNode, tokensPair: TokensPair): Trade {
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
    private fun parseOrder(node: JsonNode, tokensPair: TokensPair): Order {
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

    private fun parseOrderBook(node: JsonNode, tokensPair: TokensPair): OrdersUpdatesMessage {
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