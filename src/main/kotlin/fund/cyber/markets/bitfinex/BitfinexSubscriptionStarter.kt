package fund.cyber.markets.bitfinex

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
        val websocketHandler: BitfinexWebSocketHandler,
        val webSocketClient: WebSocketClient
) {

    @PostConstruct
    fun startSubscription() {
        val webSocketConnectionManager = WebSocketConnectionManager(webSocketClient, websocketHandler, WS_ADDRESS)
        webSocketConnectionManager.start()
    }
}