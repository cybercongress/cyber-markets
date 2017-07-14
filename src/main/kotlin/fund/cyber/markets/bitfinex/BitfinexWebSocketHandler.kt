package fund.cyber.markets.bitfinex

import fund.cyber.markets.model.ExchangeItemsReceivedMessage
import fund.cyber.markets.storage.RethinkDbService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.WebSocketMessage
import org.springframework.web.socket.WebSocketSession


@Component
open class BitfinexWebSocketHandler(
        val bitfinexMetaInformation: BitfinexMetaInformation,
        val bitfinexMessageParser: BitfinexMessageParser,
        val rethinkDbService: RethinkDbService
) : WebSocketHandler {

    private val LOG = LoggerFactory.getLogger(BitfinexWebSocketHandler::class.java)

    @Throws(Exception::class)
    override fun afterConnectionEstablished(session: WebSocketSession) {
        LOG.info("Bitfinex websocket session is started")
        session.textMessageSizeLimit = Integer.MAX_VALUE
        bitfinexMetaInformation.channelSymbolForCurrencyPair.keys.forEach { channelSymbol ->
            session.subscribeTradeChannel(channelSymbol)
        }
    }

    @Throws(Exception::class)
    override fun handleMessage(session: WebSocketSession, wsMessage: WebSocketMessage<*>) {
        val jsonMessage = wsMessage.payload.toString()
        val message = bitfinexMessageParser.parseMessage(jsonMessage)
        when (message) {
            is ExchangeItemsReceivedMessage -> rethinkDbService.saveTrades(message.trades)
            is TradeChannelSubscribed -> {
                LOG.info("Bitfinex channel ${message.currencyPair.label()} subscribed")
                bitfinexMetaInformation.tradesChannelIdForCurrencyPair.put(message.channelId, message.currencyPair)
            }
        }
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