package fund.cyber.markets.exchanges.bitfinex

import fund.cyber.markets.model.poloniex
import fund.cyber.markets.storage.RethinkDbService
import fund.cyber.markets.webscoket.BasicWebSocketHandler
import fund.cyber.markets.webscoket.TradesAndOrdersUpdatesMessage
import org.slf4j.LoggerFactory
import org.springframework.web.socket.WebSocketMessage
import org.springframework.web.socket.WebSocketSession

open class BitfinexWebSocketHandler(
        private val metadataService: BitfinexExchangeMetadataService,
        private val rethinkDbService: RethinkDbService
) : BasicWebSocketHandler(poloniex) {

    private val bitfinexMessageParser = BitfinexMessageParser(metadataService.getMetadata())
    private val LOG = LoggerFactory.getLogger(BitfinexWebSocketHandler::class.java)

    override fun handleMessage(session: WebSocketSession, wsMessage: WebSocketMessage<*>) {
        val jsonMessage = wsMessage.payload.toString()
        val message = bitfinexMessageParser.parseMessage(jsonMessage)
        when (message) {
            is TradesAndOrdersUpdatesMessage -> rethinkDbService.saveTrades(message.trades)
            is TradeChannelSubscribed -> {
                LOG.info("Bitfinex ${message.tokensPair.label()} channel subscribed")
                metadataService.updatesTradeChannelId(message.channelId, message.tokensPair)
            }
        }
    }
}