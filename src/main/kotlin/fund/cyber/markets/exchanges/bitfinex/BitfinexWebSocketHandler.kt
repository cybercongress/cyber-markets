package fund.cyber.markets.exchanges.bitfinex

import fund.cyber.markets.helpers.logger
import fund.cyber.markets.model.Trade
import fund.cyber.markets.model.poloniex
import fund.cyber.markets.webscoket.BasicWebSocketHandler
import fund.cyber.markets.webscoket.TradesAndOrdersUpdatesMessage
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.channels.SendChannel
import kotlinx.coroutines.experimental.launch
import org.springframework.web.socket.WebSocketMessage
import org.springframework.web.socket.WebSocketSession

open class BitfinexWebSocketHandler(
        private val metadataService: BitfinexExchangeMetadataService,
        private val tradesChannel: SendChannel<Trade>
) : BasicWebSocketHandler(poloniex) {

    private val bitfinexMessageParser = BitfinexMessageParser(metadataService.metadata)

    override fun handleMessage(session: WebSocketSession, wsMessage: WebSocketMessage<*>) {
        val jsonMessage = wsMessage.payload.toString()
        val message = bitfinexMessageParser.parseMessage(jsonMessage)
        
        launch(CommonPool) {
            when (message) {
                is TradesAndOrdersUpdatesMessage -> message.trades.forEach { tradesChannel.send(it) }
                is TradeChannelSubscribed -> {
                    LOGGER.info("Bitfinex ${message.tokensPair.label()} channel subscribed")
                    metadataService.updatesTradeChannelId(message.channelId, message.tokensPair)
                }
            }
        }

    }

    companion object {
        private val LOGGER = logger(BitfinexWebSocketHandler::class)
    }
}