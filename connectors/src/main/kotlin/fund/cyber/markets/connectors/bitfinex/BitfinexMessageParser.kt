package fund.cyber.markets.connectors.bitfinex

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import fund.cyber.markets.connectors.common.*
import fund.cyber.markets.model.TradeType.BUY
import fund.cyber.markets.model.TradeType.SELL
import fund.cyber.markets.connectors.common.ws.SaveExchangeMessageParser
import fund.cyber.markets.model.*
import java.math.BigDecimal
import java.util.*

private val event_property = "event"
private val channel_property = "channel"

private val event_type_subscribed = "subscribed"
private val event_type_info = "info"

private val channel_id = "chanId"
private val pair = "pair"

private val trade_executed = "te"

class BitfinexTradesMessageParser(
        channelSymbolForTokensPairInitializer: Map<String, TokensPairInitializer>,
        channelIdForTokensPairInitializer: Map<Int, TokensPairInitializer>
) : BitfinexMessageParser(channelSymbolForTokensPairInitializer, channelIdForTokensPairInitializer) {

    override fun parseUpdateMessage(jsonRoot: JsonNode): ExchangeMessage? {
        // tu ?
        // ex - [53,"te",[43334639,1499972199000,0.01293103,2320]]
        val updateType = jsonRoot[1]?.asText()
        return when (updateType) {
            trade_executed -> (parseTrade(jsonRoot))
            else -> (null)
        }
    }

    // [53,"te",[43334639,1499972199000,-0.01293103,2320]]
    // Trade node - [id, time(ms), baseAmount, rate]
    // sign of base amount determines trade type ( - sell | + buy)
    private fun parseTrade(jsonRoot: JsonNode): ExchangeMessage {

        val channelId = jsonRoot[0].asInt()
        val tokensPair = channelIdForTokensPairInitializer[channelId]
                ?: return ContainingUnknownTokensPairMessage(channelId.toString())

        val tradeNode = jsonRoot[2]
        val rate = BigDecimal(tradeNode[3].asText())
        var baseAmount = BigDecimal(tradeNode[2].asText())
        val tradeType = if (baseAmount.signum() > 0) BUY else SELL
        baseAmount = baseAmount.abs()

        val trades = Collections.singletonList(Trade.of(
                tradeId = tradeNode[0].asText(),
                exchange = Exchanges.bitfinex,
                timestamp = tradeNode[1].asLong().div(1000),
                type = tradeType,
                baseAmount = baseAmount,
                quoteAmount = rate * baseAmount,
                spotPrice = rate,
                tokensPairInitializer = tokensPair
        ))

        return TradesUpdatesMessage(trades)
    }
}

class BitfinexOrdersMessageParser(
        channelSymbolForTokensPairInitializer: Map<String, TokensPairInitializer>,
        channelIdForTokensPairInitializer: Map<Int, TokensPairInitializer>
) : BitfinexMessageParser(channelSymbolForTokensPairInitializer, channelIdForTokensPairInitializer) {

    override fun parseUpdateMessage(jsonRoot: JsonNode): ExchangeMessage? {
        val channelId = jsonRoot[0].asInt()
        val tokensPair = channelIdForTokensPairInitializer[channelId]
                ?: return ContainingUnknownTokensPairMessage(channelId.toString())

        val orders = mutableListOf<Order>()
        var ordersUpdateMessageType = OrdersUpdateType.COMMON

        // [9, [0.072038, 2, -10.003]] - for order update
        // [9, [[0.072038, 2, -10.003], ..., [0.072314, 1, -1]] - for order book snapshot
        if(jsonRoot[1][0] is ArrayNode) {
            jsonRoot[1].forEach { node ->
                orders.add(parseOrder(node, tokensPair))
            }
            ordersUpdateMessageType = OrdersUpdateType.FULL_ORDER_BOOK
        } else {
            orders.add(parseOrder(jsonRoot[1], tokensPair))
        }

        return OrdersUpdatesMessage(type = ordersUpdateMessageType, exchange = Exchanges.bitfinex,
                baseToken = tokensPair.pair.base, quoteToken = tokensPair.pair.quote, orders = orders)
    }

    // Order node - [price, count, amount]
    // sign of base amount determines trade type ( - sell | + buy)
    private fun parseOrder(jsonNode: JsonNode, tokensPairInitializer: TokensPairInitializer): Order {
        val count = jsonNode[1].asInt()
        var amount = BigDecimal(jsonNode[2].asText())
        val orderType = if (amount.signum() > 0) OrderType.SELL else OrderType.BUY
        //when count = 0 then you have to delete the price level.
        //    if amount = 1 then remove from bids
        //    if amount = -1 then remove from asks
        amount = if (count > 0) amount.abs() else BigDecimal.ZERO
        return Order(
                type = orderType,
                exchange = Exchanges.bitfinex,
                baseToken = tokensPairInitializer.pair.base,
                quoteToken = tokensPairInitializer.pair.quote,
                spotPrice = BigDecimal(jsonNode[0].asText()),
                amount = amount
        )
    }

}

/**
 *  Bitfinex ws v2 message parser.
 *
 *  @author hleb.albau@gmail.com
 */
abstract class BitfinexMessageParser(
        protected val channelSymbolForTokensPairInitializer: Map<String, TokensPairInitializer>,
        protected val channelIdForTokensPairInitializer: Map<Int, TokensPairInitializer>

) : SaveExchangeMessageParser() {

    override fun parseMessage(jsonRoot: JsonNode): ExchangeMessage? {
        val eventType = jsonRoot[event_property]?.asText()

        //ex - {"event":"subscribed","channel":"trades","chanId":53,"symbol":"tBTCUSD","pair":"BTCUSD"}
        if (eventType != null) {
            return when (eventType) {
                event_type_info -> (parseInfoEvent(jsonRoot))
                event_type_subscribed -> (parseSubscribedMessage(jsonRoot))
                else -> (null)
            }
        }

        return parseUpdateMessage(jsonRoot)
    }

    protected abstract fun parseUpdateMessage(jsonRoot: JsonNode): ExchangeMessage?

    private fun parseInfoEvent(jsonRoot: JsonNode): ExchangeMessage? {
        return null
    }

    private fun parseSubscribedMessage(jsonNode: JsonNode): ExchangeMessage? {
        val channel = jsonNode[channel_property]?.asText()
        return when (channel) {
            in listOf(channel_trades, channel_orders) -> parseChannelSubscribed(jsonNode)
            else -> null
        }
    }

    //{"event":"subscribed","channel":"trades","chanId":53,"symbol":"tBTCUSD","pair":"BTCUSD"}
    private fun parseChannelSubscribed(jsonNode: JsonNode): ExchangeMessage {
        val channelId = jsonNode[channel_id].asInt()
        val channelSymbol = jsonNode[pair].asText()
        val tokensPair = channelSymbolForTokensPairInitializer[channelSymbol]
                ?: return ContainingUnknownTokensPairMessage(channelSymbol)

        return ChannelSubscribed(channelId, tokensPair)
    }
}