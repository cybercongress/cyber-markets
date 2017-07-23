package fund.cyber.markets.exchanges

import fund.cyber.markets.exchanges.poloniex.PoloniexMetadata
import fund.cyber.markets.exchanges.poloniex.getTokensPairsWithChannelIds
import fund.cyber.markets.helpers.createExchange
import fund.cyber.markets.helpers.logger
import fund.cyber.markets.helpers.retryUntilSuccess
import fund.cyber.markets.model.ExchangeMetadata
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.future.await
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.newFixedThreadPoolContext
import org.knowm.xchange.ExchangeFactory
import org.knowm.xchange.poloniex.PoloniexExchange
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture.supplyAsync


interface Exchange<out M : ExchangeMetadata> {
    suspend fun loadMetadata(): M
}

@Component
open class PoloniexExchange : Exchange<PoloniexMetadata> {
    override suspend fun loadMetadata(): PoloniexMetadata {
        return supplyAsync {
            val poloniex = ExchangeFactory.INSTANCE.createExchange<PoloniexExchange>()
            PoloniexMetadata(channelIdForTokensPairs = poloniex.getTokensPairsWithChannelIds())
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
    private val exchanges: List<Exchange<*>>
) {

    private val jobPool = newFixedThreadPoolContext(1, "Job Pool")

    fun run() {
        exchanges.forEach { exchange ->
            launch(CommonPool) {
                delay(10000)
                try {
                    println("Loading metadata")
                    val loadMetadata = retryUntilSuccess { exchange.loadMetadata() }
                    println("Metadata loaded")
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
