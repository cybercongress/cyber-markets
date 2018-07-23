package fund.cyber.markets.cassandra.migration.repository

import fund.cyber.markets.cassandra.migration.model.CqlSchemaVersion
import org.springframework.data.cassandra.core.cql.QueryOptions
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository
import reactor.core.publisher.Flux

interface SchemaVersionRepository : ReactiveCassandraRepository<CqlSchemaVersion, String> {

    fun findAllByApplicationId(
        applicationId: String, options: QueryOptions = QueryOptions.empty()
    ): Flux<CqlSchemaVersion>
}
