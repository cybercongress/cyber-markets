package fund.cyber.markets.hitbtc

import com.fasterxml.jackson.databind.JsonNode
import fund.cyber.markets.exchanges.common.BasicWsMessageParser
import fund.cyber.markets.exchanges.common.ContainingUnknownTokensPairMessage
import fund.cyber.markets.exchanges.common.ExchangeMessage
import fund.cyber.markets.exchanges.common.TradesAndOrdersUpdatesMessage
import fund.cyber.markets.model.Trade
import fund.cyber.markets.model.TradeType
import fund.cyber.markets.model.hitbtc
import org.springframework.stereotype.Component
import java.math.BigDecimal


/**
 * HitBtc exchange ws messages parser.
 *
 * HitBtc sends instead of trade baseamount -> number of lots. Each {@link HitBtcTokensPair} contains lot size.
 *
 */
@Component
open class HitBtcMessageParser(
        val hitBtcMetaInformation: HitBtcMetaInformation
) : BasicWsMessageParser(hitbtc) {

    override fun parseMessage(jsonRoot: JsonNode): ExchangeMessage? {
        val marketDataIncrementalRefreshNode = jsonRoot["MarketDataIncrementalRefresh"] ?: return null
        return parseMarketDataIncrementalRefresh(marketDataIncrementalRefreshNode)
    }

    private fun parseMarketDataIncrementalRefresh(node: JsonNode): ExchangeMessage {

        val symbol = node["symbol"].asText()
        val tokensPair = hitBtcMetaInformation.channelSymbolForTokensPair[symbol]
                ?: return ContainingUnknownTokensPairMessage(symbol)

        val timestamp = node["timestamp"].asLong() / 1000

        val trades = node["trade"].toList()
                .map { tradeNode ->
                    val baseAmount = BigDecimal(tradeNode["size"].asText()).multiply(tokensPair.lotSize)
                    val spotPrice = BigDecimal(tradeNode["price"].asText())
                    Trade(
                            tradeId = tradeNode["tradeId"].asText(), exchange = hitbtc,
                            tokensPair = tokensPair, type = TradeType.valueOf(tradeNode["side"].asText().toUpperCase()),
                            baseAmount = baseAmount, quoteAmount = spotPrice * baseAmount,
                            spotPrice = spotPrice, timestamp = timestamp
                    )
                }
        return TradesAndOrdersUpdatesMessage(trades)
    }
}