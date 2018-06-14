package fund.cyber.markets

import fund.cyber.markets.ticker.service.TickerProcessorExecutor
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.data.cassandra.CassandraDataAutoConfiguration

@SpringBootApplication(exclude = [CassandraDataAutoConfiguration::class])
class TickersApplication {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val application = SpringApplication(TickersApplication::class.java)

            val applicationContext = application.run(*args)
            val executor = applicationContext.getBean(TickerProcessorExecutor::class.java)
            executor.start()
        }
    }

}