package fund.cyber.markets.bitfinex

import com.fasterxml.jackson.databind.JsonNode
import fund.cyber.markets.exchanges.common.BasicWsMessageParser
import fund.cyber.markets.exchanges.common.ContainingUnknownTokensPairMessage
import fund.cyber.markets.exchanges.common.ExchangeMessage
import fund.cyber.markets.exchanges.common.TradesAndOrdersUpdatesMessage
import fund.cyber.markets.model.Trade
import fund.cyber.markets.model.TradeType.BUY
import fund.cyber.markets.model.TradeType.SELL
import fund.cyber.markets.model.bitfinex
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.util.*


val event_property = "event"
val channel_property = "channel"

val event_type_subscribed = "subscribed"
val event_type_info = "info"

val channel_id = "chanId"
val channel_symbol = "symbol"

val trade_executed = "te"

/**
 *  Bitfinex ws v2 message parser.
 *
 *  @author hleb.albau@gmail.com
 */
@Component
open class BitfinexMessageParser(
        val bitfinexMetaInformation: BitfinexMetaInformation
) : BasicWsMessageParser(bitfinex) {

    override fun parseMessage(jsonRoot: JsonNode): ExchangeMessage? {
        val eventType = jsonRoot[event_property]?.asText()

        //ex - {"event":"subscribed","channel":"trades","chanId":53,"symbol":"tBTCUSD","pair":"BTCUSD"}
        if (eventType != null) {
            return when (eventType) {
                event_type_info -> parseInfoEvent(jsonRoot)
                event_type_subscribed -> parseSubscribedMessage(jsonRoot)
                else -> null
            }
        }

        // ex - [53,"te",[43334639,1499972199000,0.01293103,2320]]
        val updateType = jsonRoot[1]?.asText()
        return when (updateType) {
            trade_executed -> parseTrade(jsonRoot)
            else -> null
        }
    }

    private fun parseInfoEvent(jsonRoot: JsonNode): ExchangeMessage? {
        return null
    }

    private fun parseSubscribedMessage(jsonNode: JsonNode): ExchangeMessage? {
        val channel = jsonNode[channel_property]?.asText()
        return when (channel) {
            channel_trades -> parseTradesChannelSubscribed(jsonNode)
            channel_orders -> parseOrdersChannelSubscribed(jsonNode)
            else -> null
        }
    }

    //{"event":"subscribed","channel":"trades","chanId":53,"symbol":"tBTCUSD","pair":"BTCUSD"}
    private fun parseTradesChannelSubscribed(jsonNode: JsonNode): ExchangeMessage {
        val channelId = jsonNode[channel_id].asInt()
        val channelSymbol = jsonNode[channel_symbol].asText()
        val tokensPair = bitfinexMetaInformation.channelSymbolForTokensPair[channelSymbol]
                ?: return ContainingUnknownTokensPairMessage(channelSymbol)

        return TradeChannelSubscribed(channelId, tokensPair)
    }

    // [53,"te",[43334639,1499972199000,-0.01293103,2320]]
    // Trade node - [id, time(ms), baseAmount, rate]
    // sign of base amount determines trade type ( - sell | + buy)
    private fun parseTrade(jsonRoot: JsonNode): ExchangeMessage {

        val channelId = jsonRoot[0].asInt()
        val tokensPair = bitfinexMetaInformation.tradesChannelIdForTokensPair[channelId]
                ?: return ContainingUnknownTokensPairMessage(channelId.toString())

        val tradeNode = jsonRoot[2]
        val rate = BigDecimal(tradeNode[3].asText())
        var baseAmount = BigDecimal(tradeNode[2].asText())
        val tradeType = if (baseAmount.signum() > 0) BUY else SELL
        baseAmount = baseAmount.abs()

        val trades = Collections.singletonList(Trade(
                tradeId = tradeNode[0].asText(), exchange = bitfinex,
                tokensPair = tokensPair, type = tradeType,
                baseAmount = baseAmount, quoteAmount = rate * baseAmount,
                spotPrice = rate, timestamp = tradeNode[1].asLong().div(1000)
        ))

        return TradesAndOrdersUpdatesMessage(trades)
    }

    private fun parseOrdersChannelSubscribed(jsonNode: JsonNode): ExchangeMessage? {
        return null
    }
}