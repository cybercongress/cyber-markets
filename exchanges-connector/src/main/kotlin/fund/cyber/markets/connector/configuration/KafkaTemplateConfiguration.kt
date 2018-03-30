package fund.cyber.markets.connector.configuration

import fund.cyber.markets.configuration.KAFKA_BROKERS
import fund.cyber.markets.configuration.KAFKA_BROKERS_DEFAULT
import fund.cyber.markets.kafka.JsonSerializer
import fund.cyber.markets.model.Order
import fund.cyber.markets.model.Trade
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.knowm.xchange.dto.marketdata.OrderBook
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import java.util.*

@EnableKafka
@Configuration
class KafkaTemplateConfiguration {

    @Value("\${$KAFKA_BROKERS:$KAFKA_BROKERS_DEFAULT}")
    private lateinit var kafkaBrokers: String

    @Bean
    fun tradeProducerFactory(): ProducerFactory<String, Trade> {
        return DefaultKafkaProducerFactory(tradeProducerConfigs())
    }

    @Bean
    fun orderProducerFactory(): ProducerFactory<String, Order> {
        return DefaultKafkaProducerFactory(orderProducerConfigs())
    }

    @Bean
    fun orderBookProducerFactory(): ProducerFactory<String, OrderBook> {
        return DefaultKafkaProducerFactory(orderBookProducerConfigs())
    }

    @Bean
    fun tradeProducerConfigs(): Map<String, Any> {
        val props = HashMap<String, Any>()
        props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = kafkaBrokers
        props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = JsonSerializer<Trade>()::class.java

        return props
    }

    @Bean
    fun orderProducerConfigs(): Map<String, Any> {
        val props = HashMap<String, Any>()
        props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = kafkaBrokers
        props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = JsonSerializer<Order>()::class.java

        return props
    }

    @Bean
    fun orderBookProducerConfigs(): Map<String, Any> {
        val props = HashMap<String, Any>()
        props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = kafkaBrokers
        props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java

        //todo: use own class for orderbook
        props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = JsonSerializer<OrderBook>()::class.java

        return props
    }

    @Bean
    fun tradeKafkaTemplate(): KafkaTemplate<String, Trade> {
        return KafkaTemplate(tradeProducerFactory())
    }

    @Bean
    fun orderKafkaTemplate(): KafkaTemplate<String, Order> {
        return KafkaTemplate(orderProducerFactory())
    }

    @Bean
    fun orderBookKafkaTemplate(): KafkaTemplate<String, OrderBook> {
        return KafkaTemplate(orderBookProducerFactory())
    }

}