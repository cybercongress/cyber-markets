package fund.cyber.markets.hitbtc

import org.springframework.stereotype.Component
import org.springframework.web.socket.client.WebSocketClient
import org.springframework.web.socket.client.WebSocketConnectionManager
import javax.annotation.PostConstruct


@Component
open class HitBbcSubscriptionStarter(
        val websocketHandler: HitBtcWebSocketHandler,
        val webSocketClient: WebSocketClient
) {

    @PostConstruct
    fun startSubscription() {

        val webSocketConnectionManager = WebSocketConnectionManager(webSocketClient, websocketHandler, "ws://api.hitbtc.com:80")
        webSocketConnectionManager.start()
    }
}