package fund.cyber.markets.exchanges.poloniex

import fund.cyber.markets.exchanges.common.ws.ReconnectableWsExchange
import fund.cyber.markets.helpers.await
import fund.cyber.markets.httpClient
import fund.cyber.markets.jsonParser
import fund.cyber.markets.model.TokensPair
import io.undertow.websockets.core.WebSocketChannel
import io.undertow.websockets.core.WebSockets
import okhttp3.Request

class PoloniexExchange : ReconnectableWsExchange() {

    override val name = "Poloniex"
    override val wsAddress = "wss://api2.poloniex.com/"

    private val channelIdForTokensPairs = hashMapOf<Int, TokensPair>()
    override val messageParser = PoloniexMessageParser(channelIdForTokensPairs)


    val tickerRequest = Request.Builder().url("https://poloniex.com/public?command=returnTicker").build()!!

    override suspend fun initializeMetadata() {

        val response = httpClient.newCall(tickerRequest).await()
        val pairsTickers = jsonParser.readTree(response.body()?.string())

        pairsTickers.fields().forEach { pairTicker ->
            channelIdForTokensPairs.put(pairTicker.value["id"].asInt(), TokensPair.fromLabel(pairTicker.key, "_"))
        }
    }


    override fun subscribeChannels(connection: WebSocketChannel) {
        channelIdForTokensPairs.values.forEach { pair ->
            val symbol = pair.label("_")
            WebSockets.sendText("""{"command":"subscribe","channel":"$symbol"}""", connection, null)
        }
    }
}
