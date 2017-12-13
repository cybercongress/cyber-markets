package fund.cyber.markets.dao.service

import com.datastax.driver.core.Cluster
import com.datastax.driver.core.Session
import com.datastax.driver.mapping.Mapper
import com.datastax.driver.mapping.MappingManager
import fund.cyber.markets.model.Ticker

class TickerDaoService(cassandra: Cluster) {

    private var tickerMapper : Mapper<Ticker>? = null

    private val session: Session = cassandra.connect("markets").apply {
        val manager = MappingManager(this)
        tickerMapper = manager.mapper(Ticker::class.java)
    }

    fun insert(ticker: Ticker) {
        tickerMapper!!.saveAsync(ticker)
    }
}