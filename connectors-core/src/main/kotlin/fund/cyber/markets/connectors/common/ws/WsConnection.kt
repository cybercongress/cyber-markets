package fund.cyber.markets.connectors.common.ws

import fund.cyber.markets.connectors.byteBuffersPool
import fund.cyber.markets.connectors.xnioSsl
import fund.cyber.markets.connectors.xnioWorker
import fund.cyber.markets.helpers.cAwait
import io.undertow.websockets.client.WebSocketClient
import io.undertow.websockets.core.AbstractReceiveListener
import io.undertow.websockets.core.BufferedTextMessage
import io.undertow.websockets.core.WebSocketChannel
import io.undertow.websockets.core.WebSockets
import java.net.URI

class WsConnection(val wsAddress: String) {
    var wsChannel: WebSocketChannel? = null
    val isOpen: Boolean
        get() = wsChannel?.isOpen ?: false
    val isCloseFrameSent: Boolean
        get() = wsChannel?.isCloseFrameSent ?: false

    suspend fun establish(messageHandler: (String) -> Unit) {
        wsChannel = WebSocketClient
                .connectionBuilder(xnioWorker, byteBuffersPool, URI(wsAddress)).setSsl(xnioSsl).connect().cAwait()
        wsChannel?.idleTimeout = 60 * 1000
        wsChannel?.receiveSetter?.set(object : AbstractReceiveListener() {
            override fun onFullTextMessage(session: WebSocketChannel, message: BufferedTextMessage) {
                messageHandler(message.data ?: "")
            }
        })
        wsChannel?.resumeReceives()
    }

    fun sendTextMessage(message: String) {
        if (wsChannel != null) WebSockets.sendText(message, wsChannel, null)
    }

    fun close() = wsChannel?.close()
}
