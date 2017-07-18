package fund.cyber.markets.exchanges.poloniex

import fund.cyber.markets.model.TokensPair
import org.knowm.xchange.poloniex.PoloniexExchange
import org.knowm.xchange.poloniex.service.PoloniexMarketDataService
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession

/**
 * Poloniex exchange utilities container.
 *
 * @author hleb.albau@gmail.com
 */


val WS_ADDRESS = "wss://api2.poloniex.com/"

val HEARTBEAT_CODE = 1010
val TICKER_CODE = 1002


/**
 * Warning! Sends messages on session.
 */
fun WebSocketSession.subscribeChannel(channelId: Int) {
    sendMessage(TextMessage("{\"command\":\"subscribe\",\"channel\":\"$channelId\"}"))
}

/**
 * Warning! Invokes http request to obtain data.
 */
fun PoloniexExchange.getTokensPairsWithChannelIds(): Map<Int, TokensPair> {

    remoteInit()
    val poloniexTickers = (marketDataService as PoloniexMarketDataService).getAllPoloniexTickers()

    val channelIdForTokensPair: MutableMap<Int, TokensPair> = HashMap()
    poloniexTickers?.forEach { tokensPairSymbol, metaInfo ->
        val pairId = metaInfo.additionalProperties["id"] as Int
        channelIdForTokensPair.put(pairId, toTokensPair(tokensPairSymbol))
    }
    return channelIdForTokensPair
}

private fun toTokensPair(tokensPairSymbol: String): TokensPair {
    val split = tokensPairSymbol.split("_")
    return TokensPair(split[0], split[1])
}