package fund.cyber.markets.exchanges.hitbtc

import fund.cyber.markets.model.hitbtc
import fund.cyber.markets.storage.RethinkDbService
import fund.cyber.markets.webscoket.BasicWebSocketHandler
import fund.cyber.markets.webscoket.TradesAndOrdersUpdatesMessage
import org.springframework.web.socket.WebSocketMessage
import org.springframework.web.socket.WebSocketSession

open class HitBtcWebSocketHandler(
        private val rethinkDbService: RethinkDbService,
        metadataService: HitBtcExchangeMetadataService
) : BasicWebSocketHandler(hitbtc) {

    private val messageParser = HitBtcMessageParser(metadataService.getMetadata())

    override fun handleMessage(session: WebSocketSession?, message: WebSocketMessage<*>?) {
        val jsonMessage = message?.payload.toString()
        val exchangeMessage = messageParser.parseMessage(jsonMessage)
        when (exchangeMessage) {
            is TradesAndOrdersUpdatesMessage -> rethinkDbService.saveTrades(exchangeMessage.trades)
        }
    }
}