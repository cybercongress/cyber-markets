package fund.cyber.markets.cassandra.migration

import fund.cyber.markets.cassandra.migration.model.CqlSchemaVersion
import fund.cyber.markets.cassandra.migration.repository.SchemaVersionRepository
import fund.cyber.markets.common.readAsString
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.data.cassandra.core.ReactiveCassandraOperations
import org.springframework.stereotype.Component
import java.util.*

private const val SCHEMA_VERSION_CQL_LOCATION = "/migrations/schema_version_create.cql"

@Component
class ElassandraSchemaMigrationEngine(
    private val cassandraTemplate: ReactiveCassandraOperations,
    private val schemaVersionRepository: SchemaVersionRepository,
    private val migrationsLoader: MigrationsLoader,
    private val migrationSettings: MigrationSettings
) : InitializingBean {

    private val log = LoggerFactory.getLogger(ElassandraSchemaMigrationEngine::class.java)

    override fun afterPropertiesSet() {
        createSchemaVersionTable()
        executeSchemaUpdate(migrationsLoader.load(migrationSettings))
    }

    private fun executeSchemaUpdate(migrations: List<Migration>) {

        log.info("Executing elassandra schema update")
        log.info("Found ${migrations.size} migrations")

        migrations.filter { it !is EmptyMigration }.groupBy { m -> m.applicationId }
            .forEach { applicationId, applicationMigrations ->

                val executedMigrations = schemaVersionRepository
                    .findAllByApplicationId(applicationId)
                    .map(CqlSchemaVersion::id).collectList().block() ?: emptyList()

                applicationMigrations
                    .filter { migration -> !executedMigrations.contains(migration.id) }
                    .sortedBy { migration -> migration.id.substringBefore("_").toInt() }
                    .forEach { migration ->
                        log.info("Executing '$applicationId' application migration to '${migration.id}' id")
                        executeMigration(migration as CassandraMigration)
                        log.info("Succeeded '$applicationId' application migration to '${migration.id}' id")
                    }

            }

        log.info("Elassandra schema update done")
    }

    @Suppress("UNCHECKED_CAST")
    private fun executeMigration(migration: CassandraMigration) {

        migration.getStatements().forEach { statement ->
            cassandraTemplate.reactiveCqlOperations.execute(statement).block()
        }

        val schemeMigrationRecord = CqlSchemaVersion(
            applicationId = migration.applicationId, id = migration.id,
            apply_time = Date(), migration_hash = migration.hashCode()
        )
        schemaVersionRepository.save(schemeMigrationRecord).block()
    }

    private fun createSchemaVersionTable() {

        val createSchemaVersionCql = ElassandraSchemaMigrationEngine::class.java
            .getResourceAsStream(SCHEMA_VERSION_CQL_LOCATION).readAsString()

        cassandraTemplate.reactiveCqlOperations.execute(createSchemaVersionCql).block()
    }

}
