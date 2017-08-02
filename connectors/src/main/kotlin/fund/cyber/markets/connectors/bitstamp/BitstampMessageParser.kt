package fund.cyber.markets.connectors.bitstamp

import com.fasterxml.jackson.databind.JsonNode
import fund.cyber.markets.connectors.common.ContainingUnknownTokensPairMessage
import fund.cyber.markets.connectors.common.ExchangeMessage
import fund.cyber.markets.connectors.common.TradesAndOrdersUpdatesMessage
import fund.cyber.markets.connectors.common.ws.SaveExchangeMessageParser
import fund.cyber.markets.connectors.common.ws.pusher.getPusherEventByValue
import fund.cyber.markets.jsonParser
import fund.cyber.markets.model.TokensPair
import fund.cyber.markets.model.Trade
import fund.cyber.markets.model.TradeType
import java.math.BigDecimal
import java.util.*

/**
 * Created by aalbov on 1.8.17.
 */
private val event_type_trade = "trade"

open class BitstampMessageParser(
        private val channelIdForTokensPairs: Map<String, TokensPair>
): SaveExchangeMessageParser() {

    override fun parseMessage(jsonRoot: JsonNode): ExchangeMessage? {
        val eventType = jsonRoot["event"].asText()

        val pusherEvent = getPusherEventByValue(eventType)
        if (pusherEvent != null) {
            return pusherEvent.parseMessage(jsonRoot)
        }

        return when (eventType) {
            event_type_trade -> parseTradeEvent(jsonRoot)
            else -> null
        }
    }

    fun parseTradeEvent(jsonRoot: JsonNode): ExchangeMessage {
        val channelId = jsonRoot["channel"].asText()
        val tokensPair = channelIdForTokensPairs[channelId] ?: return ContainingUnknownTokensPairMessage(channelId)

        val trades = Collections.singletonList(parseTrade(jsonParser.readTree(jsonRoot["data"].asText()), tokensPair))

        return TradesAndOrdersUpdatesMessage(trades)
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
}
