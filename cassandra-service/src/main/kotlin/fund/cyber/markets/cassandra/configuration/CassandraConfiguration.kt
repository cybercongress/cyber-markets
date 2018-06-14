package fund.cyber.markets.cassandra.configuration

import com.datastax.driver.core.HostDistance
import com.datastax.driver.core.PoolingOptions
import fund.cyber.markets.cassandra.common.defaultKeyspaceSpecification
import fund.cyber.markets.common.CASSANDRA_HOSTS
import fund.cyber.markets.common.CASSANDRA_HOSTS_DEFAULT
import fund.cyber.markets.common.CASSANDRA_PORT
import fund.cyber.markets.common.CASSANDRA_PORT_DEFAULT
import fund.cyber.markets.common.jsonDeserializer
import fund.cyber.markets.common.jsonSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.cassandra.config.AbstractReactiveCassandraConfiguration
import org.springframework.data.cassandra.core.convert.CassandraCustomConversions
import org.springframework.data.cassandra.core.cql.keyspace.CreateKeyspaceSpecification
import org.springframework.data.cassandra.repository.config.EnableReactiveCassandraRepositories
import org.springframework.data.convert.CustomConversions

const val MAX_CONCURRENT_REQUESTS = 8182

const val ENTITY_BASE_PACKAGE = "fund.cyber.markets.cassandra.model"
const val MARKETS_KEYSPACE = "markets"

@Configuration
@EnableReactiveCassandraRepositories(
    basePackages = [ENTITY_BASE_PACKAGE]
)
class CassandraConfiguration(
    @Value("\${$CASSANDRA_HOSTS:$CASSANDRA_HOSTS_DEFAULT}")
    private val cassandraHosts: String,
    @Value("\${$CASSANDRA_PORT:$CASSANDRA_PORT_DEFAULT}")
    private val cassandraPort: Int
) : AbstractReactiveCassandraConfiguration() {

    override fun getKeyspaceName(): String = MARKETS_KEYSPACE

    override fun getEntityBasePackages(): Array<String> = arrayOf(ENTITY_BASE_PACKAGE)

    override fun getKeyspaceCreations(): List<CreateKeyspaceSpecification> {
        return listOf(defaultKeyspaceSpecification(MARKETS_KEYSPACE))
    }

    override fun getPoolingOptions() = PoolingOptions()
        .setMaxRequestsPerConnection(HostDistance.LOCAL, MAX_CONCURRENT_REQUESTS)
        .setMaxRequestsPerConnection(HostDistance.REMOTE, MAX_CONCURRENT_REQUESTS)!!

    override fun getPort() = cassandraPort
    override fun getContactPoints() = cassandraHosts

    @Bean
    override fun customConversions(): CustomConversions {
        return customTickerConversions()
    }
}

private fun customTickerConversions(): CassandraCustomConversions {
    val additionConverters = listOf(
        PricesReadConverter(jsonDeserializer), PricesWriteConverter(jsonSerializer),
        VolumesReadConverter(jsonDeserializer), VolumesWriteConverter(jsonSerializer)
    )
    return CassandraCustomConversions(additionConverters)
}