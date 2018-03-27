package fund.cyber.markets

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.data.cassandra.CassandraDataAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.web.reactive.config.EnableWebFlux

@EnableWebFlux
@SpringBootApplication(exclude = [CassandraDataAutoConfiguration::class])
class RestApiApplication

fun main(args: Array<String>) {
    runApplication<RestApiApplication>(*args)
}