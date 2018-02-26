package fund.cyber.markets.cassandra.repository

import com.datastax.driver.core.Cluster
import com.datastax.driver.mapping.MappingManager
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.JdkFutureAdapters
import fund.cyber.markets.cassandra.accessor.TickerAccessor
import fund.cyber.markets.cassandra.configuration.MARKETS_KEYSPACE
import fund.cyber.markets.cassandra.configuration.PREFERRED_CONCURRENT_REQUEST_TO_SAVE_ENTITIES_LIST
import fund.cyber.markets.cassandra.model.CqlTokenTicker
import io.reactivex.Flowable
import java.util.*

class TickerRepository(cassandra: Cluster) {

    private val session = cassandra.connect(MARKETS_KEYSPACE)
    private val manager = MappingManager(session)
    private val tickerMapper by lazy { manager.mapper(CqlTokenTicker::class.java) }
    private val tickerAccessor by lazy { manager.createAccessor(TickerAccessor::class.java) }

    fun save(ticker: CqlTokenTicker) {
        tickerMapper.save(ticker)
    }

    fun saveAll(tickers: List<CqlTokenTicker>) {
        Flowable.fromIterable(tickers)
                .buffer(PREFERRED_CONCURRENT_REQUEST_TO_SAVE_ENTITIES_LIST)
                .blockingForEach { entitiesChunk ->
                    val futures = entitiesChunk
                            .map { entity -> tickerMapper.saveAsync(entity) }
                            .map { future ->  JdkFutureAdapters.listenInPoolThread(future) }
                    Futures.allAsList(futures).get()
                }
    }

    fun getTicker(token: String, interval: Long, timestampFrom: Long): CqlTokenTicker? {
        return tickerMapper.get(token, Date(timestampFrom), interval)
    }

}