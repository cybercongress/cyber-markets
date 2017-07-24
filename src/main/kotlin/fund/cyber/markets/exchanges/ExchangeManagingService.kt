package fund.cyber.markets.exchanges

import fund.cyber.markets.applicationPool
import fund.cyber.markets.exchanges.poloniex.PoloniexMetadata
import fund.cyber.markets.exchanges.poloniex.PoloniexWebSocketHandler
import fund.cyber.markets.exchanges.poloniex.getTokensPairsWithChannelIds
import fund.cyber.markets.exchanges.poloniex.subscribeChannel
import fund.cyber.markets.helpers.createExchange
import fund.cyber.markets.helpers.logger
import fund.cyber.markets.helpers.retryUntilSuccess
import fund.cyber.markets.model.ExchangeMetadata
import fund.cyber.markets.storage.AsyncRethinkDbService
import fund.cyber.markets.webscoket.DefaultWebSocketManager
import kotlinx.coroutines.experimental.future.await
import kotlinx.coroutines.experimental.launch
import org.knowm.xchange.ExchangeFactory
import org.knowm.xchange.poloniex.PoloniexExchange
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.WebSocketSession
import java.net.URI
import java.util.concurrent.CompletableFuture.supplyAsync

interface Exchange<out M : ExchangeMetadata> {
    suspend fun loadMetadata(): M
    fun getHandler(): WebSocketHandler
    val uri: URI
    fun subscribe(wsConnection: WebSocketSession)
}

@Component
open class PoloniexExchange(
    private val asyncRethinkDbService: AsyncRethinkDbService
) : Exchange<PoloniexMetadata> {

    override fun subscribe(wsConnection: WebSocketSession) {
        metadata.channelIdForTokensPairs.values.forEach { pair ->
            wsConnection.subscribeChannel(pair)
        }
    }

    override val uri: URI
        get() = metadata.wsUri()

    lateinit var metadata: PoloniexMetadata

    override fun getHandler(): WebSocketHandler {
        return PoloniexWebSocketHandler(asyncRethinkDbService, metadata)
    }

    override suspend fun loadMetadata(): PoloniexMetadata {
        return supplyAsync {
            val poloniex = ExchangeFactory.INSTANCE.createExchange<PoloniexExchange>()
            val poloniexMetadata = PoloniexMetadata(channelIdForTokensPairs = poloniex.getTokensPairsWithChannelIds())
            metadata = poloniexMetadata
            poloniexMetadata
        }.await()
    }
}

/**
 * TODO.
 *
 * @author Ibragimov Ruslan
 */
@Component
open class ExchangeManagingService(
    private val exchanges: List<Exchange<*>>,
    private val webSocketManager: DefaultWebSocketManager
) {
    fun run() {
        exchanges.forEach { exchange ->
            launch(applicationPool) {
                try {
                    LOGGER.info("Loading metadata.")
                    retryUntilSuccess { exchange.loadMetadata() }
                    LOGGER.info("Metadata loaded.")
                    val connection = webSocketManager.newConnection()
                    val wsConnection = connection.connect(exchange.getHandler(), exchange.uri)

                    LOGGER.info("Connected.")
                    exchange.subscribe(wsConnection)
                    LOGGER.info("Subscribed.")
                    launch(applicationPool) {
                        while (true) {
                            val instant = connection.onDisconnect()
                            LOGGER.error("Disconnected: $instant")
                        }
                    }

                    launch(applicationPool) {
                        while (true) {
                            val (instant, newSession) = connection.onReconnect()

                            exchange.subscribe(newSession)

                            LOGGER.error("Reconnected: $instant")
                        }
                    }
                } catch (e: Exception) {
                    LOGGER.error("Unhandled error working with exchange ${exchange::class.simpleName}.", e)
                }
            }
        }
    }

    companion object {
        private val LOGGER = logger(ExchangeManagingService::class)
    }
}
