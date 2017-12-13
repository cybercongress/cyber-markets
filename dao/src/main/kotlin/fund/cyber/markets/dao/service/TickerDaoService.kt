package fund.cyber.markets.dao.service

import com.datastax.driver.core.Cluster
import com.datastax.driver.core.Session
import com.datastax.driver.mapping.MappingManager
import fund.cyber.markets.model.Ticker
import java.util.concurrent.TimeUnit

class TickerDaoService(cassandra: Cluster) {

    private val session: Session = cassandra.connect("markets")
    private val tickerMapper by lazy { MappingManager(session).mapper(Ticker::class.java) }

    fun insert(ticker: Ticker) {
        tickerMapper.saveAsync(ticker)
    }

    fun getTicker(base: String, quote: String, windowDuration: Long, exchange: String, timestamp: Long): Ticker? {

        val resultSet = session.execute("SELECT * FROM ticker WHERE " +
                "tokensPair={base:'$base',quote:'$quote'} " +
                "AND windowDuration=$windowDuration " +
                "AND exchange='$exchange' " +
                "AND timestampTo=$timestamp ")

        val result = tickerMapper.map(resultSet)

        return result.one()
    }

    fun getTickers(base: String, quote: String, windowDuration: Long, exchange: String, timestamp: Long, limit: Int): List<Ticker> {

        val resultSet = session.execute("SELECT * FROM ticker WHERE " +
                "tokensPair={base:'$base',quote:'$quote'} " +
                "AND windowDuration=$windowDuration " +
                "AND exchange='$exchange' " +
                "AND timestampTo>=$timestamp " +
                "LIMIT $limit")

        val result = tickerMapper.map(resultSet)

        return result.all()
    }

    fun getMinuteTicker(base: String, quote: String, exchange: String, timestamp: Long): Ticker? {
        return getTicker(base, quote, 60*1000L, exchange, timestamp)
    }

    fun getLastDayTicker(base: String, quote: String, exchange: String): Ticker? {

        val dayInMillis = TimeUnit.DAYS.toMillis(1)
        val timestamp = System.currentTimeMillis() / dayInMillis * dayInMillis

        val resultSet = session.execute("SELECT * FROM ticker WHERE " +
                "tokensPair={base:'$base',quote:'$quote'} " +
                "AND windowDuration=$dayInMillis " +
                "AND exchange='$exchange' " +
                "AND timestampTo=$timestamp ")

        val result = tickerMapper.map(resultSet)

        return result.one()
    }

}