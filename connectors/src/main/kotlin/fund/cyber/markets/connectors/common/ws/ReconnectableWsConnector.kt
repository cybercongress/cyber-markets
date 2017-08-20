package fund.cyber.markets.connectors.common.ws

import fund.cyber.markets.connectors.applicationSingleThreadContext
import fund.cyber.markets.connectors.helpers.concurrent
import fund.cyber.markets.helpers.retryUntilSuccess
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

interface WsConnector {
    val wsAddress: String
    fun connect(messageHandler: (String) -> Unit, afterConnectionEstablished: () -> Unit)
    fun sendMessage(message: String)
}

class ReconnectableWsConnector(
        override val wsAddress: String,
        val connectionDropPeriod: Long? = null
) : WsConnector {

    private val LOGGER = LoggerFactory.getLogger(ReconnectableWsConnector::class.java)!!

    val connection: WsConnection = WsConnection(wsAddress)

    override fun connect(messageHandler: (String) -> Unit, afterConnectionEstablished: () -> Unit) {
        concurrent {
            LOGGER.debug("Connecting to $wsAddress websocket endpoint")
            retryUntilSuccess { connection.establish(messageHandler) }
            LOGGER.debug("Connected to $wsAddress websocket endpoint")

            afterConnectionEstablished()

            if (connectionDropPeriod != null) reconnectEvery(connectionDropPeriod, TimeUnit.MILLISECONDS)

            concurrent {
                var connectionIsOpen = true
                while (connectionIsOpen) {
                    delay(30, TimeUnit.SECONDS)
                    if (!connection.isOpen || connection.isCloseFrameSent) {
                        LOGGER.debug("Session for $wsAddress was closed")
                        connection.close()
                        connect(messageHandler, afterConnectionEstablished)
                        connectionIsOpen = false
                    }
                }
            }
        }
    }

    override fun sendMessage(message: String) {
        if (message.isNotEmpty()) connection.sendTextMessage(message)
    }

    private fun reconnectEvery(time: Long, timeUnit: TimeUnit) {
        concurrent {
            while (true) {
                delay(time, timeUnit)
                LOGGER.debug("Force reconnection to $wsAddress websocket endpoint")
                connection.close()
            }
        }
    }
}