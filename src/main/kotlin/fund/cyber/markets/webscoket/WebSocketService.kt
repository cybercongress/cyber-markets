package fund.cyber.markets.webscoket

import fund.cyber.markets.applicationPool
import fund.cyber.markets.helpers.await
import fund.cyber.markets.helpers.logger
import fund.cyber.markets.helpers.retryUntilSuccess
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.WebSocketHttpHeaders
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.client.WebSocketClient
import java.net.URI
import java.time.Instant

interface WebSocketConnection {
    suspend fun connect(handler: WebSocketHandler, wsUri: URI): WebSocketSession
    suspend fun onDisconnect(): Instant
    suspend fun onReconnect(): Pair<Instant, WebSocketSession>
}

class DefaultWebSocketConnection(
    private val client: WebSocketClient
) : WebSocketConnection {

    private var connectionLostEventAlreadyFired = false
    private val disconnectChannel = Channel<Instant>()
    private val reconnectChannel = Channel<Pair<Instant, WebSocketSession>>()

    suspend override fun connect(handler: WebSocketHandler, wsUri: URI): WebSocketSession {
        var webSocketSession = openConnection(handler, wsUri)

        launch(applicationPool) {
            while (isActive) {
                LOGGER.info("Check is $wsUri session alive.")
                val sessionIsAlive = isSessionAlive(webSocketSession)
                LOGGER.info("Session status: $sessionIsAlive")

                if (!sessionIsAlive) {
                    reconnect(webSocketSession, handler, wsUri) { newSession ->
                        webSocketSession = newSession
                    }
                }
                delay(5000)
            }
        }

        return webSocketSession
    }

    suspend override fun onDisconnect(): Instant {
        return disconnectChannel.receive()
    }

    suspend override fun onReconnect(): Pair<Instant, WebSocketSession> {
        return reconnectChannel.receive()
    }

    private suspend fun openConnection(handler: WebSocketHandler, wsUri: URI): WebSocketSession {
        return retryUntilSuccess {
            LOGGER.info("Connecting to $wsUri exchange WebSocket endpoint.")
            val session = client.doHandshake(handler, WebSocketHttpHeaders(), wsUri).await()
            LOGGER.info("Connected to $wsUri exchange WebSocket endpoint.")
            session ?: throw RuntimeException("WebSocketSession is null.")
        }
    }

    private fun isSessionAlive(webSocketSession: WebSocketSession): Boolean {
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
            LOGGER.error("Session for ${webSocketSession.uri} exchange was not closed, by connection lost.", e)
            return false
        }
    }

    private suspend fun reconnect(
        webSocketSession: WebSocketSession,
        handler: WebSocketHandler,
        wsUri: URI,
        webSocketSupplier: WebSocketSupplier
    ) {
        LOGGER.info("Try to reconnect to $wsUri.")

        //should fire event on first exploration of connection lost
        if (!connectionLostEventAlreadyFired) {
            disconnectChannel.send(Instant.now())
            connectionLostEventAlreadyFired = true
        }

        webSocketSession.close()

        val newSession = openConnection(handler, wsUri)
        //successfully reconnected
        webSocketSupplier(newSession)
        reconnectChannel.send(Instant.now() to newSession)
        connectionLostEventAlreadyFired = false
    }

    companion object {
        private val LOGGER = logger(WebSocketContinuousConnectionManager::class)
    }
}

private val pingMessage = TextMessage("ping")

typealias WebSocketSupplier = (WebSocketSession) -> Unit
