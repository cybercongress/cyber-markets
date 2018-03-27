package fund.cyber.markets.ticker.configuration

import fund.cyber.markets.cassandra.model.CqlTokenTicker
import fund.cyber.markets.configuration.KAFKA_BROKERS
import fund.cyber.markets.configuration.KAFKA_BROKERS_DEFAULT
import fund.cyber.markets.kafka.JsonSerializer
import fund.cyber.markets.model.TokenTickerKey
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import java.util.*

@EnableKafka
@Configuration
class KafkaProducerConfiguration {

    @Value("\${$KAFKA_BROKERS:$KAFKA_BROKERS_DEFAULT}")
    private lateinit var kafkaBrokers: String

    @Bean
    fun tickerProducerConfig(): Properties {
        return Properties().apply {
            put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBrokers)
        }
    }

    @Bean
    fun tickerProducer(): KafkaProducer<TokenTickerKey, CqlTokenTicker> {
        return KafkaProducer<TokenTickerKey, CqlTokenTicker>(
                tickerProducerConfig(),
                JsonSerializer<TokenTickerKey>(),
                JsonSerializer<CqlTokenTicker>()
        )
    }

}