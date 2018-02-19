package fund.cyber.markets.cassandra.configuration

import com.datastax.driver.core.HostDistance
import com.datastax.driver.core.PoolingOptions
import fund.cyber.markets.configuration.CASSANDRA_HOSTS
import fund.cyber.markets.configuration.CASSANDRA_HOSTS_DEFAULT
import fund.cyber.markets.configuration.CASSANDRA_PORT
import fund.cyber.markets.configuration.CASSANDRA_PORT_DEFAULT
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.cassandra.ReactiveSession
import org.springframework.data.cassandra.config.AbstractReactiveCassandraConfiguration
import org.springframework.data.cassandra.repository.config.EnableReactiveCassandraRepositories


const val MAX_CONCURRENT_REQUESTS = 8182
const val CASSANDRA_KEYSPACE = "markets"
const val ENTITY_BASE_PACKAGE = "fund.cyber.markets.cassandra.model"

@Configuration
@EnableReactiveCassandraRepositories(basePackages = [ENTITY_BASE_PACKAGE])
class CassandraRepositoriesConfiguration(
        @Value("#{systemProperties['$CASSANDRA_HOSTS'] ?: '$CASSANDRA_HOSTS_DEFAULT'}")
        private val cassandraHosts: String,
        @Value("#{systemProperties['$CASSANDRA_PORT'] ?: '$CASSANDRA_PORT_DEFAULT'}")
        private val cassandraPort: Int
): AbstractReactiveCassandraConfiguration() {

    override fun getKeyspaceName(): String = CASSANDRA_KEYSPACE
    override fun getEntityBasePackages(): Array<String> = arrayOf(ENTITY_BASE_PACKAGE)

    override fun getPoolingOptions() = PoolingOptions()
            .setMaxRequestsPerConnection(HostDistance.LOCAL, MAX_CONCURRENT_REQUESTS)
            .setMaxRequestsPerConnection(HostDistance.REMOTE, MAX_CONCURRENT_REQUESTS)!!

    override fun getPort() = cassandraPort
    override fun getContactPoints() = cassandraHosts

    @Bean
    @Primary
    override fun reactiveSession(): ReactiveSession {
        return super.reactiveSession()
    }

}