package fund.cyber.markets.bitfinex

import org.knowm.xchange.ExchangeFactory
import org.knowm.xchange.bitfinex.v1.BitfinexExchange
import org.springframework.stereotype.Component
import org.springframework.web.socket.client.WebSocketClient
import org.springframework.web.socket.client.WebSocketConnectionManager
import javax.annotation.PostConstruct


/**
 * Starts subscription on bitfinex ws endpoint.
 *
 * @author hleb.albau@gmail.com
 */

@Component
open class BitfinexSubscriptionStarter(
        val bitfinexMetaInformation: BitfinexMetaInformation,
        val websocketHandler: BitfinexWebSocketHandler,
        val webSocketClient: WebSocketClient
) {

    @PostConstruct
    fun startSubscription() {

        val bitfinex = ExchangeFactory.INSTANCE.createExchange(BitfinexExchange::class.java.name) as BitfinexExchange
        bitfinexMetaInformation.channelSymbolForTokensPair = bitfinex.getChannelSymbolForTokensPair()

        val webSocketConnectionManager = WebSocketConnectionManager(webSocketClient, websocketHandler, WS_ADDRESS)
        webSocketConnectionManager.start()
    }
}