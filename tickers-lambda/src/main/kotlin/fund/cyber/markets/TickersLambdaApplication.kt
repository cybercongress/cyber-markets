package fund.cyber.markets

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.data.cassandra.CassandraDataAutoConfiguration
import org.springframework.boot.runApplication

@SpringBootApplication(exclude = [CassandraDataAutoConfiguration::class])
class TickersLambdaApplication

fun main(args: Array<String>) {
    runApplication<TickersLambdaApplication>(*args)
}