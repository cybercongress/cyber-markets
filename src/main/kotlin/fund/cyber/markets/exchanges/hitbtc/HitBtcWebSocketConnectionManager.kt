package fund.cyber.markets.exchanges.hitbtc

import fund.cyber.markets.model.Trade
import fund.cyber.markets.model.hitbtc
import fund.cyber.markets.storage.RethinkDbService
import fund.cyber.markets.webscoket.WebSocketContinuousConnectionManager
import kotlinx.coroutines.experimental.channels.SendChannel
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketSession


@Component
open class HitBtcWebSocketConnectionManager(
        metadataService: HitBtcExchangeMetadataService, tradesChannel: SendChannel<Trade>
) : WebSocketContinuousConnectionManager<HitBtcMetadata>(
        hitbtc, HitBtcWebSocketHandler(tradesChannel, metadataService), metadataService
) {

    override fun subscribeChannels(session: WebSocketSession, metadata: HitBtcMetadata) {
        //hit btc do not use channels abstraction
        //after subscribing, messages for all available pairs will be pushed
    }
}