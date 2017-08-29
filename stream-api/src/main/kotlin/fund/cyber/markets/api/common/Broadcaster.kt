package fund.cyber.markets.api.common

import io.undertow.websockets.core.WebSocketChannel

interface Broadcaster {
    fun registerChannel(channel: WebSocketChannel)
    fun unregisterChannel(channel: WebSocketChannel)
}