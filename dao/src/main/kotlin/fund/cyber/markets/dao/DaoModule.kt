package fund.cyber.markets.dao

import com.datastax.driver.core.Cluster
import fund.cyber.markets.dao.service.TickerDaoService
import java.util.*

class DaoModule(properties: Properties) {

    private val cassandraClient = Cluster.builder()
            .addContactPoints(*properties.getProperty("cassandraHost").split(",").toTypedArray())
            .withPort(properties.getProperty("cassandraPort").toInt())
            .build().init()!!

    val tickersDaoService by lazy { TickerDaoService(cassandraClient) }

    fun shutdown() {
        cassandraClient.close()
    }

}