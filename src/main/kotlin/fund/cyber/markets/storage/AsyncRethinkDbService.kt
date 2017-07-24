package fund.cyber.markets.storage

import fund.cyber.markets.applicationPool
import fund.cyber.markets.configuration.config
import fund.cyber.markets.helpers.logger
import fund.cyber.markets.model.Trade
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.actor
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture.supplyAsync

/**
 * @author Ibragimov Ruslan
 */
interface AsyncRethinkDbService {
    fun saveTrades(trades: List<Trade>)
}

@Component
open class DefaultAsyncRethinkDbService(
    private val rethinkDbService: RethinkDbService
) : AsyncRethinkDbService {

    override fun saveTrades(trades: List<Trade>) {
        launch(applicationPool) {
            saveActor.send(trades)
        }
    }

    private val saveActor = actor<List<Trade>>(applicationPool) {
        val trades = mutableListOf<Trade>()
        var task = schedule(trades)

        while (true) {
            trades += this.channel.receive()
            LOGGER.info("Trades received (${trades.size})")

            if (trades.size >= config.dataBaseConfig.batchSize) {
                LOGGER.info("Flush trades by size (${trades.size})")
                flush(trades.toList())
                trades.clear()
                task.cancel()

                task = schedule(trades)
            }
        }
    }

    private fun schedule(trades: MutableList<Trade>): Job {
        return launch(applicationPool) {
            while (isActive) {
                delay(config.dataBaseConfig.batchTime)
                if (trades.size > 0) {
                    LOGGER.info("Flush trades by time (${trades.size})")
                    flush(trades.toList())
                    trades.clear()
                }
            }
        }
    }

    private fun flush(trades: List<Trade>) {
        supplyAsync {
            rethinkDbService.saveTrades(trades)
        }
    }

    companion object {
        val LOGGER = logger(DefaultAsyncRethinkDbService::class)
    }
}
