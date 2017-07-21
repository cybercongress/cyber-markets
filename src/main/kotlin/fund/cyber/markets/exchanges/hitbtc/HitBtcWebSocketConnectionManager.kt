package fund.cyber.markets.exchanges.hitbtc

import fund.cyber.markets.model.hitbtc
import fund.cyber.markets.storage.RethinkDbService
import fund.cyber.markets.webscoket.WebSocketContinuousConnectionManager
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketSession


@Component
open class HitBtcWebSocketConnectionManager(
        metadataService: HitBtcExchangeMetadataService, rethinkDbService: RethinkDbService
) : WebSocketContinuousConnectionManager<HitBtcMetadata>(
        hitbtc, HitBtcWebSocketHandler(rethinkDbService, metadataService), metadataService
) {

    override fun subscribeChannels(session: WebSocketSession, metadata: HitBtcMetadata) {
        //hit btc do not use channels abstraction
        //after subscribing, messages for all available pairs will be pushed
    }
}