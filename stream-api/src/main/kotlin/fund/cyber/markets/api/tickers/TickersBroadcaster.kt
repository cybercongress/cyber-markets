package fund.cyber.markets.api.tickers

import com.fasterxml.jackson.databind.ObjectMapper
import fund.cyber.markets.api.common.Broadcaster
import fund.cyber.markets.api.common.StreamApiResponseMessage
import fund.cyber.markets.api.configuration.AppContext
import fund.cyber.markets.model.Ticker
import fund.cyber.markets.tickersSingleThreadContext
import io.undertow.websockets.core.WebSocketChannel
import io.undertow.websockets.core.WebSockets
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.launch

typealias TickersChannel = Channel<Ticker>

class TickersBroadcaster(
        private val newTickersChannel: TickersChannel,
        private val jsonSerializer: ObjectMapper = AppContext.jsonSerializer
) : Broadcaster {

    private var lastTicker = Ticker()
    private val registeredChannels = HashSet<WebSocketChannel>()

    init {
        launch(tickersSingleThreadContext) {
            for (ticker in newTickersChannel) {
                handleNewTicker(ticker)
            }
        }
    }

    private fun handleNewTicker(ticker: Ticker) {
        lastTicker = ticker

        if (registeredChannels.size == 0) {
            return
        }

        val tickerAsJson = jsonSerializer.writeValueAsString(
                StreamApiResponseMessage("tickers", ticker))
        launch(tickersSingleThreadContext) {
            for (channel in registeredChannels) {
                WebSockets.sendText(tickerAsJson, channel, null)
            }
        }
    }

    override fun registerChannel(channel: WebSocketChannel) {
        launch(tickersSingleThreadContext) {
            val tickerAsJson = jsonSerializer.writeValueAsString(
                    StreamApiResponseMessage("tickers", lastTicker)
            )
            WebSockets.sendText(tickerAsJson, channel, null)
            registeredChannels.add(channel)
        }
    }

    override fun unregisterChannel(channel: WebSocketChannel) {
        launch(tickersSingleThreadContext) {
            registeredChannels.remove(channel)
        }
    }
}