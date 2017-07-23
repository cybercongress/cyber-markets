package fund.cyber.markets.webscoket

import fund.cyber.markets.applicationPool
import fund.cyber.markets.configuration.config
import fund.cyber.markets.helpers.await
import fund.cyber.markets.helpers.logger
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.future.await
import kotlinx.coroutines.experimental.launch
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.WebSocketHttpHeaders
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.client.WebSocketClient
import java.util.concurrent.CompletableFuture
import kotlin.coroutines.experimental.Continuation
import kotlin.coroutines.experimental.suspendCoroutine

private val pingMessage = TextMessage("ping")

interface WebSocketConnection {
    suspend fun connect(name: String)
    suspend fun onDisconnect()
    suspend fun onReconnect()
}

class DefaultWebSocketConnection(
    private val client: WebSocketClient
) : WebSocketConnection {

    // current active session. update session only using monitor
    private var webSocketSession: WebSocketSession? = null

    private var connectionLostEventAlreadyFired = false

    suspend override fun connect(name: String) {
        openConnection(name)

        //initialize first session
        if (webSocketSession == null) {
            if (webSocketSession != null)
            return
        }

        //reconnect
        val sessionIsAlive = isSessionAlive(webSocketSession)
        if (!sessionIsAlive) {
            reconnect(lifecycle)
        }
    }

    suspend override fun onDisconnect() {

    }

    suspend override fun onReconnect() {

    }

    private suspend fun openConnection(name: String, handler: WebSocketHandler): WebSocketSession? {
        try {
            LOGGER.info("Connecting to $name exchange websocket endpoint")
            val session = client.doHandshake(webSocketHandler, WebSocketHttpHeaders(), metadata.wsUri()).await()
            LOGGER.info("Connected to $name exchange websocket endpoint")
            return session
        } catch (e: Exception) {
            LOGGER.info("Error during connection to  $name exchange websocket endpoint")
        }
    }

    companion object {
        private val LOGGER = logger(WebSocketContinuousConnectionManager::class)
    }
}

class WebSocketManager(
) {


    fun createNewConnection(name: String): WebSocketConnection {
        launch(applicationPool) {
            while (isActive) {
                checkWebSocketConnectionStatus(lifecycle, exchangeName)
                delay(config.wsPoolPeriod)
            }
        }

        return DefaultWebSocketConnection(

        )
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
            LOGGER.error("Session for $exchange exchange was not closed, by connection lost")
            return false
        }
    }

    private fun reconnect(lifecycle: WebSocketSessionLifecycle) {
        //should fire event on first exploration of connection lost
        if (!connectionLostEventAlreadyFired) {
            lifecycle.onDisconnect()
            connectionLostEventAlreadyFired = true
        }
        webSocketSession?.close()
        val newSession = openConnection()
        if (newSession != null) {
            //successfully reconnected
            webSocketSession = newSession
            lifecycle.onReconnect()
            connectionLostEventAlreadyFired = false
        }
    }


}