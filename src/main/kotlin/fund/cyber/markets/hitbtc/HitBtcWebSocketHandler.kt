package fund.cyber.markets.hitbtc

import fund.cyber.markets.exchanges.common.TradesAndOrdersUpdatesMessage
import fund.cyber.markets.storage.RethinkDbService
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.WebSocketMessage
import org.springframework.web.socket.WebSocketSession

@Component
open class HitBtcWebSocketHandler(
        val messageParser: HitBtcMessageParser,
        val rethinkDbService: RethinkDbService
) : WebSocketHandler {

    override fun afterConnectionClosed(session: WebSocketSession?, closeStatus: CloseStatus?) {
    }

    override fun handleMessage(session: WebSocketSession?, message: WebSocketMessage<*>?) {
        val jsonMessage = message?.payload.toString()
        val exchangeMessage = messageParser.parseMessage(jsonMessage)
        when (exchangeMessage) {
            is TradesAndOrdersUpdatesMessage -> rethinkDbService.saveTrades(exchangeMessage.trades)
        }
    }

    override fun afterConnectionEstablished(session: WebSocketSession) {
        session.textMessageSizeLimit = Integer.MAX_VALUE
    }

    override fun handleTransportError(session: WebSocketSession?, exception: Throwable?) {
    }

    override fun supportsPartialMessages(): Boolean {
        return false
    }
}