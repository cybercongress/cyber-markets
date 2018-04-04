package fund.cyber.markets.connector.configuration

import fund.cyber.markets.configuration.KAFKA_BROKERS
import fund.cyber.markets.configuration.KAFKA_BROKERS_DEFAULT
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.AdminClientConfig
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.common.config.TopicConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.core.KafkaAdmin
import java.util.concurrent.TimeUnit
import javax.annotation.PostConstruct

const val TRADES_TOPIC_PREFIX = "TRADES-"
const val ORDERS_TOPIC_PREFIX = "ORDERS-"

const val PARTITION_NUMBER = 1
const val REPLICATION_FACTOR = 1

@EnableKafka
@Configuration
class KafkaConfiguration {

    @Value("\${$KAFKA_BROKERS:$KAFKA_BROKERS_DEFAULT}")
    private lateinit var kafkaBrokers: String

    @Autowired
    private lateinit var connectorConfiguration: ConnectorConfiguration

    @Bean
    fun kafkaAdmin(): KafkaAdmin {
        val configs = mapOf(
                AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG to kafkaBrokers
        )
        return KafkaAdmin(configs)
    }

    @Bean
    fun topicConfigs(): Map<String, String> {
        return mapOf(
                TopicConfig.RETENTION_MS_CONFIG to TimeUnit.DAYS.toMillis(1).toString(),
                TopicConfig.CLEANUP_POLICY_CONFIG to TopicConfig.CLEANUP_POLICY_DELETE
        )
    }

    @PostConstruct
    fun createTopics() {
        val kafkaClient = AdminClient.create(kafkaAdmin().config)

        val newTopics = mutableListOf<NewTopic>()
        connectorConfiguration.exchanges.forEach { exchangeName ->
            val tradesTopic = NewTopic(TRADES_TOPIC_PREFIX + exchangeName, PARTITION_NUMBER, REPLICATION_FACTOR.toShort()).configs(topicConfigs())
            val ordersTopic = NewTopic(ORDERS_TOPIC_PREFIX + exchangeName, PARTITION_NUMBER, REPLICATION_FACTOR.toShort()).configs(topicConfigs())
            newTopics.add(tradesTopic)
            newTopics.add(ordersTopic)
        }

        kafkaClient.createTopics(newTopics)
        kafkaClient.close()
    }

}