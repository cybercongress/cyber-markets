package fund.cyber.markets

import fund.cyber.markets.ticker.processor.TickerProcessor
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.data.cassandra.CassandraDataAutoConfiguration
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration

@SpringBootApplication(exclude = [CassandraDataAutoConfiguration::class, KafkaAutoConfiguration::class])
class TickersApplication {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val application = SpringApplication(TickersApplication::class.java)

            val applicationContext = application.run(*args)
            val mainProcessor = applicationContext.getBean(TickerProcessor::class.java)
            mainProcessor.process()
        }
    }

}