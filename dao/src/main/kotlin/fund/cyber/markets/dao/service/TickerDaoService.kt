package fund.cyber.markets.dao.service

import com.datastax.driver.core.Cluster
import com.datastax.driver.core.Session
import com.datastax.driver.mapping.MappingManager
import fund.cyber.markets.model.Ticker

class TickerDaoService(cassandra: Cluster) {

    private val session: Session = cassandra.connect("ethereum").apply {
        val manager = MappingManager(this)
        manager.mapper(Ticker::class.java)
    }

    fun insert(ticker: Ticker) {
    }
}