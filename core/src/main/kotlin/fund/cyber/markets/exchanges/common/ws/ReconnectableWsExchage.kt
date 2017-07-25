package fund.cyber.markets.exchanges.common.ws

import fund.cyber.markets.byteBuffersPool
import fund.cyber.markets.exchanges.Exchange
import fund.cyber.markets.helpers.concurrent
import fund.cyber.markets.helpers.retryUntilSuccess
import fund.cyber.markets.webscoket.ContainingUnknownTokensPairMessage
import fund.cyber.markets.webscoket.ExchangeMessage
import fund.cyber.markets.webscoket.ExchangeMessageParser
import fund.cyber.markets.webscoket.TradesAndOrdersUpdatesMessage
import fund.cyber.markets.xnioSsl
import fund.cyber.markets.xnioWorker
import io.undertow.websockets.client.WebSocketClient
import io.undertow.websockets.core.AbstractReceiveListener
import io.undertow.websockets.core.BufferedTextMessage
import io.undertow.websockets.core.WebSocketChannel
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.delay
import org.slf4j.LoggerFactory
import java.net.URI
import java.util.concurrent.TimeUnit.SECONDS

interface WsExchange : Exchange {
    val wsAddress: String
}


abstract class ReconnectableWsExchange : WsExchange {

    private val LOGGER = LoggerFactory.getLogger(ReconnectableWsExchange::class.java)!!
    protected val channel = Channel<TradesAndOrdersUpdatesMessage>()

    abstract fun initializeMetadata()
    abstract fun subscribeChannels(connection: WebSocketChannel)
    protected abstract val messageParser: ExchangeMessageParser


    override fun subscribeData(): Channel<TradesAndOrdersUpdatesMessage> {

        concurrent {

            LOGGER.debug("Initialize $name metadata job is started")
            retryUntilSuccess { initializeMetadata() }
            LOGGER.debug("Initialize $name metadata job is finished")

            LOGGER.debug("Connecting to $name exchange websocket endpoint")
            val connection = retryUntilSuccess { initializeConnection() }
            LOGGER.debug("Connected to $name exchange websocket endpoint")

            subscribeChannels(connection)

            concurrent {
                var isActive = true
                while (isActive) {
                    delay(30, SECONDS)
                    if (!connection.isOpen || connection.isCloseFrameSent) {
                        LOGGER.debug("Session for $name exchange was closed")
                        connection.close()
                        subscribeData()
                        isActive = false
                    }
                }
            }
        }
        return channel
    }

    private fun initializeConnection(): WebSocketChannel {

        val connection = WebSocketClient
                .connectionBuilder(xnioWorker, byteBuffersPool, URI(wsAddress)).setSsl(xnioSsl).connect().get()
        connection.idleTimeout = 30 * 1000
        connection.receiveSetter.set(object : AbstractReceiveListener() {
            override fun onFullTextMessage(session: WebSocketChannel, message: BufferedTextMessage) {
                concurrent {
                    val result = messageParser.parseMessage(message.data)
                    when (result) {
                        is TradesAndOrdersUpdatesMessage -> if(!result.trades.isEmpty()) channel.send(result)
                        else -> handleUnknownMessage(result)
                    }
                }
            }
        })
        connection.resumeReceives()
        return connection
    }

    protected open fun handleUnknownMessage(message: ExchangeMessage) {
        if (message is ContainingUnknownTokensPairMessage) {
            LOGGER.debug("Unknown '${message.symbol}' tokens pair from $name exchange")
        }
    }
}