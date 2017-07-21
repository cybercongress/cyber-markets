package fund.cyber.markets.webscoket

import fund.cyber.markets.configuration.WS_CONNECTION_IDLE_TIMEOUT
import fund.cyber.markets.exchanges.ExchangeMetadataService
import fund.cyber.markets.model.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.event.EventListener
import org.springframework.scheduling.TaskScheduler
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.WebSocketHttpHeaders
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.client.WebSocketClient


val pingMessage = TextMessage("ping")

/**
 * WebSocket connection manager with reconnection support.
 *
 * Component lifecycle:
 *
 * 1) Initialization I: Initialization by spring di
 *
 * 2) Initialization II: Initialization by [ExchangeMetadataInitializedEvent] event
 *
 * 3) Connecting: After consuming event, periodic task [checkWebSocketConnectionStatus] scheduled with fixed
 *  [WS_CONNECTION_IDLE_TIMEOUT] delay to initialize session with exchange. Exchange can be unavailable, so session
 *  can be obtained not from first task invocation.
 *
 * 4) Monitoring: After success session initialization [checkWebSocketConnectionStatus] is used to monitoring connection
 *  status. Also [ConnectionWithExchangeIsEstablished] event is fired.
 *
 * 5) Reconnecting: If connection suddenly dropped, than on next task invocation event [ConnectionWithExchangeIsLost]
 *  will be fired, and component will try to reconnect to exchange.
 *
 * 6) Reconnected: After successful reconnection event [ConnectionWithExchangeIsReestablished] is fired,
 * and state go to 4) point "Monitoring"
 *
 */
abstract class WebSocketContinuousConnectionManager<in M : ExchangeMetadata>(
        val exchange: String,
        private val webSocketHandler: WebSocketHandler,
        private val metadataService: ExchangeMetadataService<M>
) {

    private val LOG = LoggerFactory.getLogger(WebSocketContinuousConnectionManager::class.java)

    @Autowired
    private lateinit var client: WebSocketClient
    @Autowired
    private lateinit var taskScheduler: TaskScheduler
    @Autowired
    private lateinit var eventBus: ApplicationEventPublisher

    //threads
    private val monitor = Any()
    //current active session. update session only using monitor
    private var webSocketSession: WebSocketSession? = null

    private var connectionLostEventAlreadyFired = false

    protected abstract fun subscribeChannels(session: WebSocketSession, metadata: M)

    @EventListener
    open fun initialize(event: ExchangeMetadataInitializedEvent) {
        if (event.exchange != exchange) {
            return
        }
        taskScheduler.scheduleWithFixedDelay(this::checkWebSocketConnectionStatus, WS_CONNECTION_IDLE_TIMEOUT * 1000)
    }

    private fun checkWebSocketConnectionStatus() {

        synchronized(monitor) {

            //initialize first session
            if (webSocketSession == null) {
                webSocketSession = openConnection()
                if (webSocketSession != null)
                    eventBus.publishEvent(ConnectionWithExchangeIsEstablished(exchange))
                return
            }

            //reconnect
            val sessionIsAlive = isSessionAlive(webSocketSession)
            if (!sessionIsAlive) {
                reconnect()
            }
        }
    }

    private fun isSessionAlive(webSocketSession: WebSocketSession?): Boolean {
        //not initialized yet
        if (webSocketSession == null) {
            return false
        }
        //closed automatically by jetty
        if (!webSocketSession.isOpen) {
            return false
        }
        //sometimes jetty do not invalidate session for closed abnormally connection
        //happens on dev machine during debug
        //this is debug-purposed code, try to send message,if error -> we should reconnect
        try {
            webSocketSession.sendMessage(pingMessage)
            return true
        } catch (e: Exception) {
            LOG.error("Session for $exchange exchange was not closed, by connection lost")
            return false
        }
    }

    private fun reconnect() {
        //should fire event on first exploration of connection lost
        if (!connectionLostEventAlreadyFired) {
            eventBus.publishEvent(ConnectionWithExchangeIsLost(exchange))
            connectionLostEventAlreadyFired = true
        }
        webSocketSession?.close()
        val newSession = openConnection()
        if (newSession != null) {
            //successfully reconnected
            webSocketSession = newSession
            eventBus.publishEvent(ConnectionWithExchangeIsReestablished(exchange))
            connectionLostEventAlreadyFired = false
        }
    }

    private fun openConnection(): WebSocketSession? {

        LOG.info("Connecting to $exchange exchange websocket endpoint")

        val metadata = metadataService.getMetadata()
        val newSessionFuture = client.doHandshake(webSocketHandler, WebSocketHttpHeaders(), metadata.wsUri())

        newSessionFuture.addCallback(
                { session ->
                    LOG.info("Connected to $exchange exchange websocket endpoint")
                    subscribeChannels(session, metadata)
                },
                { error ->
                    LOG.info("Error during connection to  $exchange exchange websocket endpoint")
                }
        )

        return newSessionFuture.get()
    }
}


