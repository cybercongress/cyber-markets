package fund.cyber.markets.storer

import fund.cyber.markets.cassandra.model.CqlOrderBook
import fund.cyber.markets.cassandra.repository.OrderBookRepository
import fund.cyber.markets.common.closestSmallerMultiply
import fund.cyber.markets.common.rest.service.ConnectorService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

@Component
class OrderBookStorer(
    private val orderBookResolver: OrderBookResolverTask,
    private val orderBookSnapshotPeriod: Long,
    private val saveOrderBooks: Boolean
) {
    private val log = LoggerFactory.getLogger(javaClass)!!
    private val scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(1)

    fun start() {
        if (saveOrderBooks) {
            scheduler.scheduleAtFixedRate(orderBookResolver, initDelay(), orderBookSnapshotPeriod, TimeUnit.MILLISECONDS)
        } else {
            log.info("Saving of order books to the database was disabled.")
        }
    }

    fun initDelay(): Long {
        val currentMillis = System.currentTimeMillis()
        return closestSmallerMultiply(currentMillis, orderBookSnapshotPeriod) + orderBookSnapshotPeriod - currentMillis
    }
}

@Component
class OrderBookResolverTask(
    private val connectorService: ConnectorService,
    private val orderBookRepository: OrderBookRepository
) : Runnable {
    private val log = LoggerFactory.getLogger(javaClass)!!

    override fun run() {
        log.info("Saving order book snapshots")

        connectorService.getExchanges().flatMap { exchange ->

            connectorService.getTokensPairsByExchange(exchange).flatMap { pair ->

                connectorService.getOrderBook(exchange, pair).flatMap { orderBook ->

                    orderBookRepository.save(CqlOrderBook(exchange, pair, orderBook))

                }
            }

        }.collectList().block()

    }

}