package fund.cyber.markets.cassandra.configuration

import com.datastax.driver.core.HostDistance
import com.datastax.driver.core.PoolingOptions
import fund.cyber.markets.cassandra.common.defaultKeyspaceSpecification
import fund.cyber.markets.common.CASSANDRA_HOSTS
import fund.cyber.markets.common.CASSANDRA_HOSTS_DEFAULT
import fund.cyber.markets.common.CASSANDRA_PORT
import fund.cyber.markets.common.CASSANDRA_PORT_DEFAULT
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.cassandra.ReactiveSession
import org.springframework.data.cassandra.config.AbstractReactiveCassandraConfiguration
import org.springframework.data.cassandra.core.cql.keyspace.CreateKeyspaceSpecification
import org.springframework.data.cassandra.repository.config.EnableReactiveCassandraRepositories

const val MAX_CONCURRENT_REQUESTS = 8182
const val MAX_PER_ROUTE = 16
const val MAX_TOTAL = 32

const val ENTITY_BASE_PACKAGE = "fund.cyber.markets.cassandra.model"
const val MARKETS_KEYSPACE = "markets"

@Configuration
@EnableReactiveCassandraRepositories(
    basePackages = [ENTITY_BASE_PACKAGE]
)
class CassandraRepositoriesConfiguration(
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
    @Primary
    override fun reactiveSession(): ReactiveSession {
        return super.reactiveSession()
    }

}

/*//val Chain.keyspace: String get() = lowerCaseName

const val REPOSITORY_NAME_DELIMETER = "__"

fun mappingContext(cluster: Cluster, keyspace: String, basePackage: String): CassandraMappingContext {

    val mappingContext = CassandraMappingContext()

    mappingContext.setInitialEntitySet(CassandraEntityClassScanner.scan(basePackage))
    mappingContext.setUserTypeResolver(SimpleUserTypeResolver(cluster, keyspace))

    return mappingContext
}

fun getKeyspaceSession(cluster: Cluster,
                       keyspace: String,
                       converter: MappingCassandraConverter) = CassandraSessionFactoryBean()
    .apply {
        setCluster(cluster)
        setConverter(converter)
        setKeyspaceName(keyspace)
        schemaAction = SchemaAction.NONE
    }

@Configuration
class CassandraConfiguration {
    private val defaultHttpHeaders = listOf(BasicHeader("Keep-Alive", "timeout=10, max=1024"))
    private val connectionManager = PoolingHttpClientConnectionManager().apply {
        defaultMaxPerRoute = MAX_PER_ROUTE
        maxTotal = MAX_TOTAL
    }

    @Bean
    fun httpClient() = HttpClients.custom()
        .setConnectionManager(connectionManager)
        .setConnectionManagerShared(true)
        .setDefaultHeaders(defaultHttpHeaders)
        .build()!!

    @Bean
    fun migrationsLoader(resourceLoader: GenericApplicationContext) = DefaultMigrationsLoader(
        resourceLoader = resourceLoader
    )
}*/