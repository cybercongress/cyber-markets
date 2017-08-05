package fund.cyber.markets.api.trades

import com.fasterxml.jackson.databind.ObjectMapper
import fund.cyber.markets.api.configuration.AppContext
import fund.cyber.markets.applicationSingleThreadContext
import fund.cyber.markets.common.CircularQueue
import fund.cyber.markets.model.Trade
import io.undertow.websockets.core.WebSocketChannel
import io.undertow.websockets.core.WebSockets
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.launch

typealias TradesChannel = Channel<Trade>

class TokensPairTradesBroadcaster(
        private val newTradesChannel: TradesChannel,
        private val jsonSerializer: ObjectMapper = AppContext.jsonSerializer
) {

    private val lastTrades = CircularQueue<Trade>(10)
    private val registeredChannels = HashSet<WebSocketChannel>()

    init {
        launch(applicationSingleThreadContext) {
            for (trade in newTradesChannel) {
                handleNewTrade(trade)
            }
        }
    }


    private fun handleNewTrade(trade: Trade) {

        lastTrades.addNext(trade)
        if (registeredChannels.size == 0) {
            return
        }

        val tradeAsJson = jsonSerializer.writeValueAsString(trade)
        launch(applicationSingleThreadContext) {
            for (channel in registeredChannels) {
                WebSockets.sendText(tradeAsJson, channel, null)
            }
        }
    }


    fun registerChannel(channel: WebSocketChannel) {

        launch(applicationSingleThreadContext) {
            val lastTradesAsJson = jsonSerializer.writeValueAsString(lastTrades.elements)
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