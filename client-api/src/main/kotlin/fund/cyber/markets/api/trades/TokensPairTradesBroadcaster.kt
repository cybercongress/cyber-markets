package fund.cyber.markets.api.trades

import fund.cyber.markets.applicationSingleThreadContext
import fund.cyber.markets.common.CircularQueue
import io.undertow.websockets.core.WebSocketChannel
import io.undertow.websockets.core.WebSockets
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.launch


class TokensPairTradesBroadcaster(private val newTradesChannel: ReceiveChannel<String>) {

    private val lastTrades = CircularQueue<String>(10)
    private val registeredChannels = HashSet<WebSocketChannel>()

    init {
        launch(applicationSingleThreadContext) {
            for (trade in newTradesChannel) {
                handleNewTrade(trade)
            }
        }
    }


    private fun handleNewTrade(jsonTrade: String) {

        lastTrades.addNext(jsonTrade)
        if (registeredChannels.size == 0) {
            return
        }

        launch(applicationSingleThreadContext) {
            for (channel in registeredChannels) {
                WebSockets.sendText(jsonTrade, channel, null)
            }
        }
    }


    fun registerChannel(channel: WebSocketChannel) {

        launch(applicationSingleThreadContext) {
            val lastTradesAsJson = lastTrades.elements.joinToString(prefix = "[", postfix = "]", separator = ",")
            WebSockets.sendText(lastTradesAsJson, channel, null)
            registeredChannels.add(channel)
        }
    }

    fun unregisterChannel(channel: WebSocketChannel) {
        launch(applicationSingleThreadContext) {
            registeredChannels.remove(channel)
        }
    }
}