package fund.cyber.markets.dao

import com.datastax.driver.core.Cluster
import fund.cyber.markets.dao.service.TickerDaoService
import java.util.*

class DaoModule(properties: Properties) {

    var tickersDaoService: TickerDaoService? = null

    init {
        val cassandraClient = Cluster.builder().addContactPoint(properties.getProperty("cassandraHost")).build().init()!!
        tickersDaoService = TickerDaoService(cassandraClient)
    }

}