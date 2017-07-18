package fund.cyber.markets.exchanges.hitbtc

import fund.cyber.markets.storage.RethinkDbService
import fund.cyber.markets.webscoket.WebSocketContinuousConnectionManager
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.client.WebSocketClient


@Component
open class HitBtcWebSocketConnectionManager(
        client: WebSocketClient,
        private val rethinkDbService: RethinkDbService
) : WebSocketContinuousConnectionManager<HitBtcMetadata>(client) {

    override fun setupWebSocketHandler(metadata: HitBtcMetadata): WebSocketHandler {
        val messageParser = HitBtcMessageParser(metadata)
        return HitBtcWebSocketHandler(messageParser, rethinkDbService)
    }
}