package fund.cyber.markets.cassandra.repository

import com.datastax.driver.core.Cluster
import com.datastax.driver.mapping.MappingManager
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.JdkFutureAdapters
import fund.cyber.markets.cassandra.MARKETS_KEYSPACE
import fund.cyber.markets.cassandra.PREFERRED_CONCURRENT_REQUEST_TO_SAVE_ENTITIES_LIST
import fund.cyber.markets.cassandra.accessor.TickerAccessor
import fund.cyber.markets.common.Durations
import fund.cyber.markets.helpers.addHop
import fund.cyber.markets.helpers.closestSmallerMultiply
import fund.cyber.markets.model.Ticker
import fund.cyber.markets.model.TokensPair
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

    fun getTicker24h(pair: TokensPair, exchange: String): Ticker? {
        val timestamp = closestSmallerMultiply(System.currentTimeMillis(), Durations.MINUTE) - Durations.DAY
        val tickers = tickerAccessor.getTickers(pair, Durations.MINUTE, exchange, Date(timestamp), Int.MAX_VALUE).all()

        if (tickers.isEmpty()) {
            return null
        }

        val tickerFirst = tickers.first()
        val ticker24h = Ticker(
            tickerFirst.pair,
                Durations.DAY,
                tickerFirst.exchange,
                tickers.last().timestampTo,
                tickerFirst.timestampFrom
        )
        tickers.forEach { ticker ->
            ticker24h addHop ticker
        }

        return ticker24h
    }

}