package fund.cyber.markets.storer

import fund.cyber.markets.cassandra.model.CqlOrderBook
import fund.cyber.markets.cassandra.repository.OrderBookRepository
import fund.cyber.markets.common.closestSmallerMultiply
import fund.cyber.markets.storer.configuration.ORDERBOOK_SNAPSHOT_PERIOD
import fund.cyber.markets.storer.service.ConnectorService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

@Component
class OrderBookStorer {

    @Autowired
    private lateinit var orderBookResolver: OrderBookResolverTask

    private val scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(1)

    fun start() {
        scheduler.scheduleAtFixedRate(orderBookResolver, initDelay(), ORDERBOOK_SNAPSHOT_PERIOD, TimeUnit.MILLISECONDS)
    }

    fun initDelay(): Long {
        val currentMillis = System.currentTimeMillis()
        return closestSmallerMultiply(currentMillis, ORDERBOOK_SNAPSHOT_PERIOD) + ORDERBOOK_SNAPSHOT_PERIOD - currentMillis
    }
}

@Component
class OrderBookResolverTask(
    private val connectorService: ConnectorService,
    private val orderBookRepository: OrderBookRepository
): Runnable {
    private val log = LoggerFactory.getLogger(javaClass)!!

    override fun run() {
        log.info("Saving order book snapshots")

        val exchanges = connectorService.getExchanges()
        if (exchanges == null) {
            log.error("Cannot get list of exchanges")
            return
        }

        exchanges.forEach { exchange ->
            connectorService.getTokensPairsByExchange(exchange)?.forEach { pair ->
                val orderBook = connectorService.getOrderBook(exchange, pair)

                if (orderBook != null) {
                    orderBookRepository.save(CqlOrderBook(exchange, pair, orderBook))
                }
            }
        }

    }

}