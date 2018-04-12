package fund.cyber.markets.ticker.configuration

import fund.cyber.markets.cassandra.model.CqlTokenTicker
import fund.cyber.markets.common.Durations
import fund.cyber.markets.common.KAFKA_BROKERS
import fund.cyber.markets.common.KAFKA_BROKERS_DEFAULT
import fund.cyber.markets.common.kafka.JsonDeserializer
import fund.cyber.markets.common.model.TokenTickerKey
import fund.cyber.markets.common.model.Trade
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.requests.IsolationLevel
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.transaction.annotation.EnableTransactionManagement
import java.util.*

@EnableKafka
@Configuration
@EnableTransactionManagement
class KafkaConsumerConfiguration {

    @Value("\${$KAFKA_BROKERS:$KAFKA_BROKERS_DEFAULT}")
    private lateinit var kafkaBrokers: String

    @Bean
    fun tradesConsumerConfig(): Properties {
        return Properties().apply {
            put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBrokers)
            put(ConsumerConfig.METADATA_MAX_AGE_CONFIG, Durations.MINUTE)
            put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, MAX_POLL_RECORDS_PROPERTY)
            put(ConsumerConfig.GROUP_ID_CONFIG, "TRADE_CONSUMER")
            put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, IsolationLevel.READ_COMMITTED.toString().toLowerCase())
            put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
        }
    }

    @Bean
    fun tickersBackupConsumerConfig(): Properties {
        return Properties().apply {
            put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBrokers)
            put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, MAX_POLL_RECORDS_PROPERTY)
            put(ConsumerConfig.GROUP_ID_CONFIG, "TICKER_BACKUP_CONSUMER")
            put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, IsolationLevel.READ_COMMITTED.toString().toLowerCase())
            put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
        }
    }

    @Bean
    fun tradesConsumer(): KafkaConsumer<String, Trade> {
        return KafkaConsumer<String, Trade>(
                tradesConsumerConfig(),
                JsonDeserializer(String::class.java),
                JsonDeserializer(Trade::class.java)
        ).apply {
            subscribe(TRADES_TOPIC_NAME_PATTERN)
        }
    }

    @Bean
    fun tickerBackupConsumer(): KafkaConsumer<TokenTickerKey, CqlTokenTicker> {
        return KafkaConsumer<TokenTickerKey, CqlTokenTicker>(
                tickersBackupConsumerConfig(),
                JsonDeserializer(TokenTickerKey::class.java),
                JsonDeserializer(CqlTokenTicker::class.java)
        ).apply {
            subscribe(listOf(TICKERS_BACKUP_TOPIC_NAME))
        }
    }

}