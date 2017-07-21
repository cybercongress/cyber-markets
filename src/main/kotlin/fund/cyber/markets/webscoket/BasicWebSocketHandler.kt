package fund.cyber.markets.webscoket

import org.slf4j.LoggerFactory
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.WebSocketSession

abstract class BasicWebSocketHandler(val exchange: String) : WebSocketHandler {

    private val LOG = LoggerFactory.getLogger(BasicWebSocketHandler::class.java)

    override fun supportsPartialMessages(): Boolean = false
    override fun afterConnectionEstablished(session: WebSocketSession) {}
    override fun afterConnectionClosed(session: WebSocketSession?, closeStatus: CloseStatus?) {}

    override fun handleTransportError(session: WebSocketSession, exception: Throwable) {
        LOG.info("$exchange exchange websocket transport error", exception)
    }
}