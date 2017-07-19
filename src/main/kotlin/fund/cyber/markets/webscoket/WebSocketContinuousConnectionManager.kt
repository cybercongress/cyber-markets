package fund.cyber.markets.webscoket

import fund.cyber.markets.model.ExchangeMetadata
import fund.cyber.markets.service.ExchangeMetadataInitializedEvent
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.event.EventListener
import org.springframework.scheduling.TaskScheduler
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.WebSocketHttpHeaders
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.client.WebSocketClient

val webSocketConnectionAliveInterval: Long = 5 * 1000

abstract class WebSocketContinuousConnectionManager<in M : ExchangeMetadata> {

    private val LOG = LoggerFactory.getLogger(WebSocketContinuousConnectionManager::class.java)

    @Autowired
    private lateinit var client: WebSocketClient
    @Autowired
    private lateinit var taskScheduler: TaskScheduler
    // initialized by event
    private lateinit var metadata: M

    //threads
    private val monitor = Any()
    //current active session
    private var webSocketSession: WebSocketSession? = null


    protected abstract fun setupWebSocketHandler(metadata: M): WebSocketHandler

    @EventListener
    private fun initialize(exchangeMetadataInitializedEvent: ExchangeMetadataInitializedEvent<M>) {

        metadata = exchangeMetadataInitializedEvent.metadata
        taskScheduler.scheduleWithFixedDelay(this::checkWebSocketConnectionStatus, webSocketConnectionAliveInterval)
    }

    private fun checkWebSocketConnectionStatus() {

        synchronized(monitor) {

            //initialize first session
            if (webSocketSession == null) {
                webSocketSession = openConnection(metadata)
                return
            }

            //reconnect
            val sessionIsAlive = webSocketSession?.isOpen ?: false
            if (!sessionIsAlive) {
                webSocketSession?.close()
                webSocketSession = openConnection(metadata)
            }
        }
    }

    private fun openConnection(metadata: M): WebSocketSession? {
        LOG.info("Try to connect to ${metadata.exchange} exchange websocket endpoint")

        val webSocketHandler = setupWebSocketHandler(metadata)
        val newSessionFuture = client.doHandshake(webSocketHandler, WebSocketHttpHeaders(), metadata.wsUri())

        newSessionFuture.addCallback(
                { success -> LOG.info("Connected to ${metadata.exchange} exchange websocket endpoint") },
                { error -> LOG.info("Error during connection to  ${metadata.exchange} exchange websocket endpoint", error) }
        )

        return newSessionFuture.get()
    }
}


