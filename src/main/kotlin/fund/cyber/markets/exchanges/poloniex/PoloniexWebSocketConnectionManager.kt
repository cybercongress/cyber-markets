package fund.cyber.markets.exchanges.poloniex

import fund.cyber.markets.model.TokensPair
import fund.cyber.markets.model.Trade
import fund.cyber.markets.model.poloniex
import fund.cyber.markets.webscoket.WebSocketContinuousConnectionManager
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.SendChannel
import org.springframework.stereotype.Component
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession


@Component
open class PoloniexWebSocketConnectionManager(
        tradesChannel: SendChannel<Trade>, metadataService: PoloniexExchangeMetadataService
) : WebSocketContinuousConnectionManager<PoloniexMetadata>(
        poloniex, PoloniexWebSocketHandler(tradesChannel, metadataService), metadataService
) {

    override fun subscribeChannels(session: WebSocketSession, metadata: PoloniexMetadata) {
        metadata.channelIdForTokensPairs.values.forEach { pair -> session.subscribeChannel(pair) }
    }
}

/**
 * Warning! Sends messages on session.
 */
fun WebSocketSession.subscribeChannel(pair: TokensPair) {
    sendMessage(TextMessage("{\"command\":\"subscribe\",\"channel\":\"${pair.base}_${pair.quote}\"}"))
}