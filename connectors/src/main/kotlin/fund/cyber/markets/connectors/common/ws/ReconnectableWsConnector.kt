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
    val name: String
    fun connect(messageHandler: (String) -> Unit)
    fun subscribeChannel(channel: String)
    fun reconnectEvery(time: Long, timeUnit: TimeUnit)
}

class ReconnectableWsConnector(
        override val wsAddress: String,
        override val name: String = ""
) : WsConnector {

    private val LOGGER = LoggerFactory.getLogger(ReconnectableWsConnector::class.java)!!

    val connection: WsConnection = WsConnection(wsAddress)
    val subscriptionChannels: MutableCollection<String> = mutableListOf()

    override fun connect(messageHandler: (String) -> Unit) {
        concurrent {
            LOGGER.debug("Connecting to $name websocket endpoint")
            retryUntilSuccess { connection.establish(messageHandler) }
            LOGGER.debug("Connected to $name websocket endpoint")

            handleSubscriptions(subscriptionChannels)

            concurrent {
                var connectionIsOpen = true
                while (connectionIsOpen) {
                    delay(30, TimeUnit.SECONDS)
                    if (!connection.isOpen || connection.isCloseFrameSent) {
                        LOGGER.debug("Session for $name was closed")
                        connection.close()
                        connect(messageHandler)
                        connectionIsOpen = false
                    }
                }
            }
        }
    }

    override fun reconnectEvery(time: Long, timeUnit: TimeUnit) {
        concurrent {
            while (true) {
                delay(time, timeUnit)
                LOGGER.debug("Force reconnection to $name websocket endpoint")
                connection.close()
            }
        }
    }

    override fun subscribeChannel(channel: String) {
        launch(applicationSingleThreadContext) {
            subscriptionChannels.add(channel)
            handleSubscriptions(listOf(channel))
        }
    }

    private fun handleSubscriptions(subscriptions: Collection<String>) {
        launch(applicationSingleThreadContext) {
            subscriptions.forEach {
                if (connection.isOpen && !connection.isCloseFrameSent) {
                    connection.sendTextMessage(it)
                }
            }
        }
    }
}