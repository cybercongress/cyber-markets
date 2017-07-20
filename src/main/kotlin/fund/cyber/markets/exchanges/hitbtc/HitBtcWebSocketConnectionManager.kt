package fund.cyber.markets.exchanges.hitbtc

import fund.cyber.markets.storage.RethinkDbService
import fund.cyber.markets.webscoket.WebSocketContinuousConnectionManager
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.WebSocketSession


@Component
open class HitBtcWebSocketConnectionManager(
        private val rethinkDbService: RethinkDbService
) : WebSocketContinuousConnectionManager<HitBtcMetadata>() {

    override fun setupWebSocketHandler(metadata: HitBtcMetadata): WebSocketHandler {
        val messageParser = HitBtcMessageParser(metadata)
        return HitBtcWebSocketHandler(messageParser, rethinkDbService)
    }

    override fun setupChannels(session: WebSocketSession, metadata: HitBtcMetadata) {
        //hit btc do not use channels abstraction
        //after subscribing, messages for all available pairs will be pushed
    }
}