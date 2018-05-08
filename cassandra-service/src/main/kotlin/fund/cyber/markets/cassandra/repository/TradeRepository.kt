package fund.cyber.markets.cassandra.repository

import com.datastax.driver.core.Cluster
import com.datastax.driver.mapping.MappingManager
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.JdkFutureAdapters
import fund.cyber.markets.cassandra.configuration.MARKETS_KEYSPACE
import fund.cyber.markets.cassandra.configuration.PREFERRED_CONCURRENT_REQUEST_TO_SAVE_ENTITIES_LIST
import fund.cyber.markets.cassandra.model.CqlTokensPair
import fund.cyber.markets.cassandra.model.CqlTrade
import io.reactivex.Flowable

class TradeRepository(cassandra: Cluster) {

    private val session = cassandra.connect(MARKETS_KEYSPACE)
    private val manager = MappingManager(session)
    private val mapper by lazy { manager.mapper(CqlTrade::class.java) }

    fun save(trade: CqlTrade) {
        mapper.save(trade)
    }

    fun saveAll(trades: List<CqlTrade>) {
        Flowable.fromIterable(trades)
                .buffer(PREFERRED_CONCURRENT_REQUEST_TO_SAVE_ENTITIES_LIST)
                .blockingForEach { entitiesChunk ->
                    val futures = entitiesChunk
                            .map { entity -> mapper.saveAsync(entity) }
                            .map { future ->  JdkFutureAdapters.listenInPoolThread(future) }
                    Futures.allAsList(futures).get()
                }
    }

    fun get(exchange: String, pair: CqlTokensPair, epochHour: Long, tradeId: String): CqlTrade? {
        return mapper.get(exchange, pair, epochHour, tradeId)
    }

}