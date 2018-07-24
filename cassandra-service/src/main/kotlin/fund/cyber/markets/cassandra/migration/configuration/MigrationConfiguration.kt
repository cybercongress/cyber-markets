package fund.cyber.markets.cassandra.migration.configuration

import fund.cyber.markets.cassandra.migration.DefaultMigrationsLoader
import fund.cyber.markets.cassandra.migration.MigrationSettings
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.GenericApplicationContext

@Configuration
class MigrationConfiguration {

    @Bean
    fun migrationsLoader(resourceLoader: GenericApplicationContext) = DefaultMigrationsLoader(
        resourceLoader = resourceLoader
    )

    @Bean
    fun migrationSettings() = MigrationSettings("markets", "markets-app")
}
