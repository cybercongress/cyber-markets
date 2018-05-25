package fund.cyber.markets.ticker.configuration.kafka

import fund.cyber.markets.common.KAFKA_BROKERS
import fund.cyber.markets.common.KAFKA_BROKERS_DEFAULT
import org.apache.kafka.clients.admin.AdminClientConfig
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.common.config.TopicConfig
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.core.KafkaAdmin
import java.util.concurrent.TimeUnit

const val TRADES_TOPIC_NAME_PATTERN = "TRADES-.*"
const val TICKERS_TOPIC_NAME = "TICKERS"
const val TICKERS_BACKUP_TOPIC_NAME = "TICKERS-BACKUP"

private const val NUMBER_PARTITIONS = 1
private const val REPLICATION_FACTOR = 1

@EnableKafka
@Configuration
class KafkaConfiguration(
    @Value("\${$KAFKA_BROKERS:$KAFKA_BROKERS_DEFAULT}")
    private val kafkaBrokers: String
) {

    @Bean
    fun kafkaAdmin(): KafkaAdmin {
        val configs = mapOf(
            AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG to kafkaBrokers
        )
        return KafkaAdmin(configs)
    }

    @Bean
    fun tickersTopic(): NewTopic {
        return NewTopic(TICKERS_TOPIC_NAME, NUMBER_PARTITIONS, REPLICATION_FACTOR.toShort()).configs(topicConfigs())
    }

    @Bean
    fun tickersBackupTopic(): NewTopic {
        return NewTopic(TICKERS_BACKUP_TOPIC_NAME, NUMBER_PARTITIONS, REPLICATION_FACTOR.toShort()).configs(topicConfigs())
    }

    @Bean
    fun topicConfigs(): Map<String, String> {
        return mapOf(
            TopicConfig.RETENTION_MS_CONFIG to TimeUnit.DAYS.toMillis(1).toString(),
            TopicConfig.CLEANUP_POLICY_CONFIG to TopicConfig.CLEANUP_POLICY_DELETE
        )
    }

}