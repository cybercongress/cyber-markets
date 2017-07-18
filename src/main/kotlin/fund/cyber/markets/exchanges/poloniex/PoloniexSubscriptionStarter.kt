package fund.cyber.markets.exchanges.poloniex

import org.springframework.stereotype.Component
import org.springframework.web.socket.client.WebSocketClient
import org.springframework.web.socket.client.WebSocketConnectionManager

/**
 * Starts subscription on poloniex ws endpoint.
 *
 * @author hleb.albau@gmail.com
 */

@Component
open class PoloniexSubscriptionStarter(
        val poloniexWebSocketHandler: PoloniexWebSocketHandler,
        val webSocketClient: WebSocketClient
) {

    fun startSubscription() {
        val webSocketConnectionManager = WebSocketConnectionManager(webSocketClient, poloniexWebSocketHandler, WS_ADDRESS)
        webSocketConnectionManager.start()
    }
}