package fund.cyber.markets.exchanges.poloniex

import com.fasterxml.jackson.databind.JsonNode
import fund.cyber.markets.model.TokensPair
import fund.cyber.markets.model.Trade
import fund.cyber.markets.model.TradeType.BUY
import fund.cyber.markets.model.TradeType.SELL
import fund.cyber.markets.webscoket.ContainingUnknownTokensPairMessage
import fund.cyber.markets.webscoket.ExchangeMessage
import fund.cyber.markets.webscoket.SaveExchangeMessageParser
import fund.cyber.markets.webscoket.TradesAndOrdersUpdatesMessage
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

    override fun parseMessage(jsonRoot: JsonNode): ExchangeMessage? {
        val channelId = jsonRoot.get(0).asInt()
        val tokensPair = channelIdForTokensPairs[channelId]
                ?: return ContainingUnknownTokensPairMessage(channelId.toString())

        val trades = jsonRoot[2].toList()
                .filter { node -> node[0].asText() == "t" }
                .map { node -> parseTrade(node, tokensPair) }

        return TradesAndOrdersUpdatesMessage(trades)
    }

    //["t","126320",1,"0.00003328","399377.76875000",1499708547]
    //["t", id, sell/buy,  rate,      quantity,        time(s) ]
    private fun parseTrade(node: JsonNode, tokensPair: TokensPair): Trade {
        val spotPrice = BigDecimal(node[3].asText())
        val baseAmount = BigDecimal(node[4].asText())
        return Trade(
                tradeId = node[1].asText(), exchange = "Poloniex",
                baseToken = tokensPair.base, quoteToken = tokensPair.quote,
                type = if (node[2].asInt() == 0) SELL else BUY,
                baseAmount = baseAmount, quoteAmount = spotPrice * baseAmount,
                spotPrice = spotPrice, timestamp = node[5].asLong()
        )
    }
}