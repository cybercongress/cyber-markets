package fund.cyber.markets.ticker.configuration.kafka

import fund.cyber.markets.common.KAFKA_BROKERS
import fund.cyber.markets.common.KAFKA_BROKERS_DEFAULT
import fund.cyber.markets.common.kafka.JsonDeserializer
import fund.cyber.markets.common.model.Trade
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory

private const val MAX_POLL_RECORDS = 1000

@EnableKafka
@Configuration
class KafkaListenerConfiguration(
    @Value("\${$KAFKA_BROKERS:$KAFKA_BROKERS_DEFAULT}")
    private val kafkaBrokers: String
) {

    @Bean
    fun kafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, Trade> {
        return ConcurrentKafkaListenerContainerFactory<String, Trade>().apply {
            consumerFactory = consumerFactory()
            isBatchListener = true
        }
    }

    @Bean
    fun consumerFactory(): ConsumerFactory<String, Trade> {
        return DefaultKafkaConsumerFactory<String, Trade>(consumerConfigs()).apply {
            keyDeserializer = JsonDeserializer(String::class.java)
            valueDeserializer = JsonDeserializer(Trade::class.java)
        }
    }

    @Bean
    fun consumerConfigs(): Map<String, Any> = mutableMapOf(
        ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to kafkaBrokers,
        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
        ConsumerConfig.MAX_POLL_RECORDS_CONFIG to MAX_POLL_RECORDS
    )
}