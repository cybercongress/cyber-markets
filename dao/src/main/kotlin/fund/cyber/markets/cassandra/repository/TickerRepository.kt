package fund.cyber.markets.cassandra.repository

import com.datastax.driver.core.Cluster
import com.datastax.driver.mapping.MappingManager
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.JdkFutureAdapters
import fund.cyber.markets.cassandra.MARKETS_KEYSPACE
import fund.cyber.markets.cassandra.PREFERRED_CONCURRENT_REQUEST_TO_SAVE_ENTITIES_LIST
import fund.cyber.markets.cassandra.accessor.TickerAccessor
import fund.cyber.markets.common.Durations
import fund.cyber.markets.dto.TokensPair
import fund.cyber.markets.model.Ticker
import io.reactivex.Flowable
import java.util.*

class TickerRepository(cassandra: Cluster) {

    private val session = cassandra.connect(MARKETS_KEYSPACE)
    private val manager = MappingManager(session)
    private val tickerMapper by lazy { manager.mapper(Ticker::class.java) }
    private val tickerAccessor by lazy { manager.createAccessor(TickerAccessor::class.java) }

    fun save(ticker: Ticker) {
        tickerMapper.save(ticker)
    }

    fun saveAll(tickers: List<Ticker>) {
        Flowable.fromIterable(tickers)
                .buffer(PREFERRED_CONCURRENT_REQUEST_TO_SAVE_ENTITIES_LIST)
                .blockingForEach { entitiesChunk ->
                    val futures = entitiesChunk
                            .map { entity -> tickerMapper.saveAsync(entity) }
                            .map { future ->  JdkFutureAdapters.listenInPoolThread(future) }
                    Futures.allAsList(futures).get()
                }
    }

    fun getTicker(pair: TokensPair, windowDuration: Long, exchange: String, timestamp: Long): Ticker? {
        return tickerMapper.get(pair, windowDuration, exchange, Date(timestamp))
    }

    fun getTickers(pair: TokensPair, windowDuration: Long, exchange: String, timestamp: Long, limit: Int): List<Ticker> {
        return tickerAccessor.getTickers(pair, windowDuration, exchange, Date(timestamp), limit).all()
    }

    fun getMinuteTicker(pair: TokensPair, exchange: String, timestamp: Long): Ticker? {
        return getTicker(pair, Durations.MINUTE, exchange, timestamp)
    }

    fun getLastDayTicker(pair: TokensPair, exchange: String): Ticker? {
        val windowDuration = Durations.DAY
        val timestamp = System.currentTimeMillis() / windowDuration * windowDuration

        return tickerMapper.get(pair, windowDuration, exchange, Date(timestamp))
    }

}