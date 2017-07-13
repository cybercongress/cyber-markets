package fund.cyber.markets.poloniex

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import fund.cyber.markets.model.CurrencyPair
import fund.cyber.markets.model.ExchangeItems
import fund.cyber.markets.model.Trade
import fund.cyber.markets.model.TradeType
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.Instant

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


@Component
open class PoloniexMessageParser(
        val poloniexMetaInformation: PoloniexMetaInformation
) {

    private val mapper = ObjectMapper()

    fun parseMessage(message: String): ExchangeItems {

        val newItems = ExchangeItems()

        val jsonNode = mapper.readTree(message)
        val channelId = jsonNode.get(0).asInt()
        val currencyPair = poloniexMetaInformation.channelIdForCurrencyPair[channelId]

        //if currencyPair != null -> we get message for 0..1000 channels
        if (currencyPair != null) {
            // iterate through data array
            jsonNode.get(2).elements().forEach { node ->
                if ("t" == node[0].asText()) {
                    newItems.trades.add(parseTrade(node, currencyPair))
                }
            }
        }
        return newItems
    }

    //["t","126320",1,"0.00003328","399377.76875000",1499708547]
    //["t", id, sell/buy,  rate,      quantity,        time(s) ]
    private fun parseTrade(node: JsonNode, currencyPair: CurrencyPair): Trade {
        val rate = BigDecimal(node[3].asText())
        val quantity = BigDecimal(node[4].asText())
        return Trade(
                id = node[1].asText(),
                exchange = "Poloniex",
                currencyPair = currencyPair,
                type = if (node[2].asInt() == 0) TradeType.SELL else TradeType.BUY,
                rate = rate,
                quantity = quantity,
                total = rate * quantity,
                timestamp = Instant.ofEpochSecond(node[5].asLong(), 0)
        )
    }
}