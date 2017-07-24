package fund.cyber.markets.webscoket

import org.springframework.stereotype.Component
import org.springframework.web.socket.client.WebSocketClient

/**
 * @author Ibragimov Ruslan
 */
interface WebSocketManager {
    fun newConnection(): WebSocketConnection
}

@Component
open class DefaultWebSocketManager(
    private val client: WebSocketClient
) : WebSocketManager {
    override fun newConnection() = DefaultWebSocketConnection(client)
}