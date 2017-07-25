package fund.cyber.markets.exchanges.poloniex

import fund.cyber.markets.exchanges.common.ws.ReconnectableWsExchange
import fund.cyber.markets.model.TokensPair
import io.undertow.websockets.core.WebSocketChannel
import io.undertow.websockets.core.WebSockets

class PoloniexExchange : ReconnectableWsExchange() {

    override val name = "Poloniex"
    override val wsAddress = "wss://api2.poloniex.com/"

    private val channelIdForTokensPairs = hashMapOf<Int, TokensPair>()
    override val messageParser = PoloniexMessageParser(channelIdForTokensPairs)


    override fun initializeMetadata() {
        channelIdForTokensPairs.put(148, TokensPair("BTC", "ETH"))
    }

    override fun subscribeChannels(connection: WebSocketChannel) {
        channelIdForTokensPairs.values.forEach { pair ->
            val symbol = pair.label("_")
            WebSockets.sendText("""{"command":"subscribe","channel":"$symbol"}""", connection, null)
        }
    }
}
