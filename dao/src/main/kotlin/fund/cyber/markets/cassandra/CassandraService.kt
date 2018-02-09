package fund.cyber.markets.cassandra

import com.datastax.driver.core.Cluster
import fund.cyber.markets.cassandra.repository.SupplyRepository
import fund.cyber.markets.cassandra.repository.TickerRepository
import fund.cyber.markets.cassandra.repository.VolumeRepository
import java.util.*

const val MAX_CONCURRENT_REQUESTS = 8182
const val PREFERRED_CONCURRENT_REQUEST_TO_SAVE_ENTITIES_LIST = MAX_CONCURRENT_REQUESTS / 8

const val CASSANDRA_HOST_PROPERTY = "cassandraHost"
const val CASSANDRA_PORT_PROPERTY = "cassandraPort"

const val MARKETS_KEYSPACE = "markets"

class CassandraService(properties: Properties) {

    private val cassandraClient = Cluster.builder()
            .addContactPoints(*properties.getProperty(CASSANDRA_HOST_PROPERTY).split(",").toTypedArray())
            .withPort(properties.getProperty(CASSANDRA_PORT_PROPERTY).toInt())
            .build().init()!!

    val tickerRepository by lazy { TickerRepository(cassandraClient) }
    val volumeRepository by lazy { VolumeRepository(cassandraClient) }
    val supplyRepository by lazy { SupplyRepository(cassandraClient) }

    fun shutdown() {
        cassandraClient.close()
    }

}