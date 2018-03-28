package fund.cyber.markets

import fund.cyber.markets.connector.ConnectorRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.data.cassandra.CassandraDataAutoConfiguration
import javax.annotation.PreDestroy

@SpringBootApplication(exclude = [CassandraDataAutoConfiguration::class])
class ExchangesConnectorApplication {

    companion object {
        private lateinit var connectorRunner: ConnectorRunner

        @JvmStatic
        fun main(args: Array<String>) {
            val application = SpringApplication(ExchangesConnectorApplication::class.java)
            val applicationContext = application.run(*args)

            connectorRunner = applicationContext.getBean(ConnectorRunner::class.java)
            connectorRunner.start()
        }

        @PreDestroy
        fun shutdown() {
            connectorRunner.shutdown()
        }
    }

}