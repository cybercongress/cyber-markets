package fund.cyber.markets.cassandra.repository

import com.datastax.driver.core.Cluster
import com.datastax.driver.mapping.MappingManager
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.JdkFutureAdapters
import fund.cyber.markets.cassandra.accessor.OrderBookAccessor
import fund.cyber.markets.cassandra.configuration.MARKETS_KEYSPACE
import fund.cyber.markets.cassandra.configuration.PREFERRED_CONCURRENT_REQUEST_TO_SAVE_ENTITIES_LIST
import fund.cyber.markets.cassandra.model.CqlOrderBook
import fund.cyber.markets.cassandra.model.CqlTokensPair
import fund.cyber.markets.common.model.TokensPair
import io.reactivex.Flowable
import java.util.*

class OrderBookRepository(cassandra: Cluster) {

    private val session = cassandra.connect(MARKETS_KEYSPACE)
    private val manager = MappingManager(session)
    private val mapper by lazy { manager.mapper(CqlOrderBook::class.java) }
    private val accessor by lazy { manager.createAccessor(OrderBookAccessor::class.java) }

    fun save(orderBook: CqlOrderBook) {
        mapper.save(orderBook)
    }

    fun saveAll(orderBooks: List<CqlOrderBook>) {
        Flowable.fromIterable(orderBooks)
                .buffer(PREFERRED_CONCURRENT_REQUEST_TO_SAVE_ENTITIES_LIST)
                .blockingForEach { entitiesChunk ->
                    val futures = entitiesChunk
                            .map { entity -> mapper.saveAsync(entity) }
                            .map { future ->  JdkFutureAdapters.listenInPoolThread(future) }
                    Futures.allAsList(futures).get()
                }
    }

    fun get(exchange: String, pair: CqlTokensPair, epochHour: Long, timestamp: Long): CqlOrderBook? {
        return mapper.get(exchange, pair, epochHour, Date(timestamp))
    }

    fun getNearlest(exchange: String, pair: TokensPair, epochHour: Long, timestamp: Long): CqlOrderBook? {
        return accessor.getNearlest(exchange, CqlTokensPair(pair), epochHour, Date(timestamp)).firstOrNull()
    }

}