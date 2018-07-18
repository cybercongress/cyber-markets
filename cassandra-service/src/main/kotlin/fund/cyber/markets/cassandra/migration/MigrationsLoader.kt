package fund.cyber.markets.cassandra.migration

import fund.cyber.markets.common.readAsString
import org.springframework.context.support.GenericApplicationContext
import org.springframework.core.io.Resource

interface MigrationsLoader {
    fun load(settings: MigrationSettings): List<Migration>
}

private const val CQL_EXTENSION = "cql"

class DefaultMigrationsLoader(
    private val migrationsRootDirectory: String = "migrations",
    private val resourceLoader: GenericApplicationContext
) : MigrationsLoader {

    override fun load(settings: MigrationSettings): List<Migration> {

        return resourceLoader
            .getResources("classpath*:/$migrationsRootDirectory/${settings.migrationDirectory}/*.*")
            .toList()
            .map { resource -> createMigration(resource, settings) }
    }

    private fun createMigration(resource: Resource, migrationSettings: MigrationSettings): Migration {

        val extension = resource.filename?.substringAfterLast(".")
        val nameWithoutExtension = resource.filename?.substringBeforeLast(".") ?: return EmptyMigration()
        val content = resource.inputStream.readAsString()

        return when (extension) {
            CQL_EXTENSION -> CqlFileBasedMigration(nameWithoutExtension, migrationSettings.applicationId, content)
            else -> EmptyMigration()
        }
    }
}
