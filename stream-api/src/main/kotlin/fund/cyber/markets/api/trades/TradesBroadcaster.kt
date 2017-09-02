package fund.cyber.markets.api.trades

import com.fasterxml.jackson.databind.ObjectMapper
import fund.cyber.markets.api.common.Broadcaster
import fund.cyber.markets.api.configuration.AppContext
import fund.cyber.markets.common.CircularQueue
import fund.cyber.markets.helpers.rand
import fund.cyber.markets.model.Trade
import fund.cyber.markets.tradesSingleThreadContext
import io.undertow.websockets.core.WebSocketChannel
import io.undertow.websockets.core.WebSockets
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.launch

typealias TradesChannel = Channel<Trade>

class TradesBroadcaster(
    private val newTradesChannel: TradesChannel,
    private val jsonSerializer: ObjectMapper = AppContext.jsonSerializer
) : Broadcaster {

    private val lastTrades = CircularQueue<Trade>(10)
    private val registeredChannels = HashSet<WebSocketChannel>()

    init {
        launch(tradesSingleThreadContext) {
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
        launch(tradesSingleThreadContext) {
            for (channel in registeredChannels) {
                WebSockets.sendText(tradeAsJson, channel, null)
            }
        }
    }

    override fun registerChannel(channel: WebSocketChannel) {
        launch(tradesSingleThreadContext) {
            registeredChannels.add(channel)
        }
    }

    fun getRandomTradeFromBroadcaster(): Trade? {
        return lastTrades[rand(0, lastTrades.elements.size)]
    }

    override fun unregisterChannel(channel: WebSocketChannel) {
        launch(tradesSingleThreadContext) {
            registeredChannels.remove(channel)
        }
    }
}