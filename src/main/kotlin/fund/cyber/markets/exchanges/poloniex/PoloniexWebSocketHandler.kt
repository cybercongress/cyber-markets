package fund.cyber.markets.exchanges.poloniex

import fund.cyber.markets.model.Trade
import fund.cyber.markets.model.poloniex
import fund.cyber.markets.webscoket.BasicWebSocketHandler
import fund.cyber.markets.webscoket.TradesAndOrdersUpdatesMessage
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.channels.SendChannel
import kotlinx.coroutines.experimental.launch
import org.springframework.web.socket.WebSocketMessage
import org.springframework.web.socket.WebSocketSession


open class PoloniexWebSocketHandler(
        private val tradesChannel: SendChannel<Trade>,
        metadataService: PoloniexExchangeMetadataService
) : BasicWebSocketHandler(poloniex) {

    private val poloniexMessageParser = PoloniexMessageParser(metadataService.metadata)

    override fun handleMessage(session: WebSocketSession, message: WebSocketMessage<*>) {
        val jsonMessage = message.payload.toString()
        val exchangeMessage = poloniexMessageParser.parseMessage(jsonMessage)
        launch(CommonPool) {
            when (exchangeMessage) {
                is TradesAndOrdersUpdatesMessage -> exchangeMessage.trades.forEach { tradesChannel.send(it) }
            }
        }
    }
}
