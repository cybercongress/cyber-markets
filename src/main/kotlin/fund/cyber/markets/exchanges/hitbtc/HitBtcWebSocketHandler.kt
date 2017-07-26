package fund.cyber.markets.exchanges.hitbtc

import fund.cyber.markets.model.Trade
import fund.cyber.markets.model.hitbtc
import fund.cyber.markets.storage.RethinkDbService
import fund.cyber.markets.webscoket.BasicWebSocketHandler
import fund.cyber.markets.webscoket.TradesAndOrdersUpdatesMessage
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.channels.SendChannel
import kotlinx.coroutines.experimental.launch
import org.springframework.web.socket.WebSocketMessage
import org.springframework.web.socket.WebSocketSession

open class HitBtcWebSocketHandler(
        private val tradesChannel: SendChannel<Trade>,
        metadataService: HitBtcExchangeMetadataService
) : BasicWebSocketHandler(hitbtc) {

    private val messageParser = HitBtcMessageParser(metadataService.metadata)

    override fun handleMessage(session: WebSocketSession?, message: WebSocketMessage<*>?) {
        val jsonMessage = message?.payload.toString()
        val exchangeMessage = messageParser.parseMessage(jsonMessage)
        launch(CommonPool) {
            when (exchangeMessage) {
                is TradesAndOrdersUpdatesMessage -> exchangeMessage.trades.forEach { tradesChannel.send(it) }
            }
        }
    }
}