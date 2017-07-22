package fund.cyber.markets.webscoket

import fund.cyber.markets.helpers.logger
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.WebSocketSession

abstract class BasicWebSocketHandler(val exchange: String) : WebSocketHandler {

    override fun supportsPartialMessages(): Boolean = false
    override fun afterConnectionEstablished(session: WebSocketSession) {}
    override fun afterConnectionClosed(session: WebSocketSession?, closeStatus: CloseStatus?) {}

    override fun handleTransportError(session: WebSocketSession, exception: Throwable) {
        LOGGER.info("$exchange exchange websocket transport error", exception)
    }

    companion object {
        private val LOGGER = logger(BasicWebSocketHandler::class)
    }
}