package fund.cyber.markets.api.common

import io.undertow.websockets.WebSocketConnectionCallback
import io.undertow.websockets.core.AbstractReceiveListener
import io.undertow.websockets.core.WebSocketChannel
import io.undertow.websockets.spi.WebSocketHttpExchange


class RootWebSocketHandler(
        private val incomingMessagesHandler: AbstractReceiveListener
) : WebSocketConnectionCallback {

    override fun onConnect(exchange: WebSocketHttpExchange, channel: WebSocketChannel) {
        channel.getReceiveSetter().set(incomingMessagesHandler)
        channel.resumeReceives()
    }
}