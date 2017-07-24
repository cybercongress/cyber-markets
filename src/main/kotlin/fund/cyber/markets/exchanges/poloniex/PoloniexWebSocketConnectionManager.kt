package fund.cyber.markets.exchanges.poloniex

import fund.cyber.markets.model.TokensPair
import fund.cyber.markets.model.poloniex
import fund.cyber.markets.storage.AsyncRethinkDbService
import fund.cyber.markets.webscoket.WebSocketContinuousConnectionManager
import org.springframework.stereotype.Component
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession


@Component
open class PoloniexWebSocketConnectionManager(
    asyncRethinkDbService: AsyncRethinkDbService,
    metadataService: PoloniexExchangeMetadataService
) : WebSocketContinuousConnectionManager<PoloniexMetadata>(
        poloniex, PoloniexWebSocketHandler(asyncRethinkDbService, metadataService.metadata), metadataService
) {

    override fun subscribeChannels(session: WebSocketSession, metadata: PoloniexMetadata) {
        metadata.channelIdForTokensPairs.values.forEach { pair -> session.subscribeChannel(pair) }
    }
}

/**
 * Warning! Sends messages on session.
 */
fun WebSocketSession.subscribeChannel(channelId: Int) {
    sendMessage(TextMessage(""""{"command":"subscribe","channel":"$channelId"}"""))
}

fun WebSocketSession.subscribeChannel(pair: TokensPair) {
    sendMessage(TextMessage("""{"command":"subscribe","channel":"${pair.base}_${pair.quote}"}"""))
}