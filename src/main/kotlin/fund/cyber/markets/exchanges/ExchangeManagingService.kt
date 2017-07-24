package fund.cyber.markets.exchanges

import fund.cyber.markets.applicationPool
import fund.cyber.markets.exchanges.poloniex.PoloniexMetadata
import fund.cyber.markets.exchanges.poloniex.PoloniexWebSocketHandler
import fund.cyber.markets.exchanges.poloniex.getTokensPairsWithChannelIds
import fund.cyber.markets.helpers.createExchange
import fund.cyber.markets.helpers.logger
import fund.cyber.markets.helpers.retryUntilSuccess
import fund.cyber.markets.model.ExchangeMetadata
import fund.cyber.markets.storage.RethinkDbService
import fund.cyber.markets.webscoket.DefaultWebSocketManager
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.future.await
import kotlinx.coroutines.experimental.launch
import org.knowm.xchange.ExchangeFactory
import org.knowm.xchange.poloniex.PoloniexExchange
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketHandler
import java.net.URI
import java.util.concurrent.CompletableFuture.supplyAsync

interface Exchange<out M : ExchangeMetadata> {
    suspend fun loadMetadata(): M
    fun getHandler(): WebSocketHandler
    val uri: URI
}

@Component
open class PoloniexExchange(
    private val rethinkDbService: RethinkDbService
) : Exchange<PoloniexMetadata> {

    override val uri: URI
        get() = metadata.wsUri()

    lateinit var metadata: PoloniexMetadata

    override fun getHandler(): WebSocketHandler {
        return PoloniexWebSocketHandler(rethinkDbService, metadata)
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
                delay(10000)
                try {
                    println("Loading metadata")
                    retryUntilSuccess { exchange.loadMetadata() }
                    println("Metadata loaded")
                    val connection = webSocketManager.newConnection()
                    connection.connect(exchange.getHandler(), exchange.uri)

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
