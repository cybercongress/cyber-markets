package fund.cyber.markets.cassandra.configuration

import com.datastax.driver.core.Cluster
import fund.cyber.markets.cassandra.repository.SupplyRepository
import fund.cyber.markets.cassandra.repository.TickerRepository
import fund.cyber.markets.configuration.CASSANDRA_HOSTS
import fund.cyber.markets.configuration.CASSANDRA_HOSTS_DEFAULT
import fund.cyber.markets.configuration.CASSANDRA_PORT
import fund.cyber.markets.configuration.CASSANDRA_PORT_DEFAULT
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

const val MAX_CONCURRENT_REQUESTS = 8182
const val PREFERRED_CONCURRENT_REQUEST_TO_SAVE_ENTITIES_LIST = MAX_CONCURRENT_REQUESTS / 8
const val MARKETS_KEYSPACE = "markets"

@Configuration
class CassandraRepositoryConfiguration(
        @Value("#{systemProperties['${CASSANDRA_HOSTS}'] ?: '${CASSANDRA_HOSTS_DEFAULT}'}")
        private val cassandraHosts: String,
        @Value("#{systemProperties['${CASSANDRA_PORT}'] ?: '${CASSANDRA_PORT_DEFAULT}'}")
        private val cassandraPort: Int
) {
    @Bean
    fun cassandraCluster(): Cluster {
        return Cluster.builder()
                .addContactPoints(cassandraHosts)
                .withPort(cassandraPort)
                .build().init()!!
    }

    @Bean
    fun tickerRepository(): TickerRepository {
        return TickerRepository(cassandraCluster())
    }

    @Bean
    fun supplyRepository(): SupplyRepository {
        return SupplyRepository(cassandraCluster())
    }
}