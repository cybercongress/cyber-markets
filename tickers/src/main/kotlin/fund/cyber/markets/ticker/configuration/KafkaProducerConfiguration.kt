package fund.cyber.markets.ticker.configuration

import fund.cyber.markets.cassandra.model.CqlTokenTicker
import fund.cyber.markets.configuration.KAFKA_BROKERS
import fund.cyber.markets.configuration.KAFKA_BROKERS_DEFAULT
import fund.cyber.markets.kafka.JsonSerializer
import fund.cyber.markets.model.TokenTickerKey
import org.apache.kafka.clients.producer.ProducerConfig
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.transaction.KafkaTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement

@EnableKafka
@Configuration
@EnableTransactionManagement
class KafkaProducerConfiguration {

    @Value("\${$KAFKA_BROKERS:$KAFKA_BROKERS_DEFAULT}")
    private lateinit var kafkaBrokers: String

    @Bean
    fun producerFactory(): ProducerFactory<TokenTickerKey, CqlTokenTicker> {
        return DefaultKafkaProducerFactory<TokenTickerKey, CqlTokenTicker>(
                producerConfigs(),
                JsonSerializer(),
                JsonSerializer()).apply {
            setTransactionIdPrefix("TICKER_")
        }
    }

    @Bean
    fun backupProducerFactory(): ProducerFactory<TokenTickerKey, CqlTokenTicker> {
        return DefaultKafkaProducerFactory<TokenTickerKey, CqlTokenTicker>(
                producerConfigs(),
                JsonSerializer(),
                JsonSerializer()).apply {
            setTransactionIdPrefix("TICKER_BACKUP_")
        }
    }

    @Bean
    fun tickerKafkaTemplate(): KafkaTemplate<TokenTickerKey, CqlTokenTicker> {
        return KafkaTemplate(producerFactory())
    }

    @Bean
    fun tickerBackupKafkaTemplate(): KafkaTemplate<TokenTickerKey, CqlTokenTicker> {
        return KafkaTemplate(backupProducerFactory())
    }

    @Bean
    fun producerConfigs(): Map<String, Any> = mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to kafkaBrokers
    )

    @Bean
    fun transactionManager(): KafkaTransactionManager<TokenTickerKey, CqlTokenTicker> {
        return KafkaTransactionManager(producerFactory())
    }

}