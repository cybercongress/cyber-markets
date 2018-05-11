package fund.cyber.markets

import fund.cyber.markets.storer.OrderBookStorer
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.data.cassandra.CassandraDataAutoConfiguration


@SpringBootApplication(exclude = [CassandraDataAutoConfiguration::class])
class StorerApplication {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val application = SpringApplication(StorerApplication::class.java)

            val applicationContext = application.run(*args)
            val orderBookStorer = applicationContext.getBean(OrderBookStorer::class.java)
            orderBookStorer.start()
        }
    }

}