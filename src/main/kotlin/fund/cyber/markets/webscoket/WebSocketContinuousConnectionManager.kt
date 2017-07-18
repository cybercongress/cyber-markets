package fund.cyber.markets.webscoket

import fund.cyber.markets.model.ExchangeMetadata
import fund.cyber.markets.service.ExchangeMetadataInitializedEvent
import org.springframework.context.event.EventListener
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.client.WebSocketClient
import org.springframework.web.socket.client.WebSocketConnectionManager


abstract class WebSocketContinuousConnectionManager<in M : ExchangeMetadata>(
        private val client: WebSocketClient
) {

    @EventListener
    fun initialize(exchangeMetadataInitializedEvent: ExchangeMetadataInitializedEvent<M>) {
        val metadata = exchangeMetadataInitializedEvent.metadata
        val connectionManager = WebSocketConnectionManager(client, setupWebSocketHandler(metadata), metadata.wsAddress)
        connectionManager.start()
    }

    protected abstract fun setupWebSocketHandler(metadata: M): WebSocketHandler
}


