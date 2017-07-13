package fund.cyber.markets.bitfinex

import fund.cyber.markets.storage.RethinkDbService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.WebSocketMessage
import org.springframework.web.socket.WebSocketSession


@Component
open class BitfinexWebSocketHandler(
        val bitfinexMessageParser: BitfinexMessageParser,
        val rethinkDbService: RethinkDbService
) : WebSocketHandler {

    private val LOG = LoggerFactory.getLogger(BitfinexWebSocketHandler::class.java)

    @Throws(Exception::class)
    override fun afterConnectionEstablished(session: WebSocketSession) {
        LOG.info("Bitfinex websocket session is started")
        session.textMessageSizeLimit = Integer.MAX_VALUE
    }

    @Throws(Exception::class)
    override fun handleMessage(session: WebSocketSession, message: WebSocketMessage<*>) {
        val jsonMessage = message.payload.toString()
        val newExchangeItems = bitfinexMessageParser.parseMessage(jsonMessage)
        rethinkDbService.saveTrades(newExchangeItems.trades)
    }

    @Throws(Exception::class)
    override fun handleTransportError(session: WebSocketSession, exception: Throwable) {
        LOG.info("Bitfinex websocket transport error", exception)
    }

    @Throws(Exception::class)
    override fun afterConnectionClosed(session: WebSocketSession, closeStatus: CloseStatus) {
        LOG.info("Bitfinex websocket session is closed")
    }

    override fun supportsPartialMessages(): Boolean {
        return false
    }
}