package fund.cyber.markets.tickers.configuration

import fund.cyber.markets.common.Constants
import fund.cyber.markets.helpers.env
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.AdminClientConfig
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.config.TopicConfig
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

private val tradesTopicNamePattern = Pattern.compile("TRADES-.*")

class TickersConfiguration(
        val kafkaServers: String = env(Constants.KAFKA_CONNECTION, "localhost:9092"),
        val topicNamePattern: Pattern = tradesTopicNamePattern,
        val topicResubscribe: Long = TimeUnit.MINUTES.toMillis(1),
        val windowDurationsString: String = env(Constants.WINDOW_DURATIONS_MIN, "1,5,15,30,60,180,240,360,720,1440"),
        val windowHop: Long = TimeUnit.SECONDS.toMillis(env(Constants.WINDOW_HOP_SEC, 3)),
        val tickersTopicName: String = "TICKERS",
        val debug: Boolean = env("DEBUG", false)
) {

    val windowDurations = windowDurationsString.split(",").map { it -> it.toLong() * 60 * 1000 }

    val consumerProperties = Properties().apply {
        put(ConsumerConfig.METADATA_MAX_AGE_CONFIG, topicResubscribe)
        put("bootstrap.servers", kafkaServers)
        put("group.id", "TRADE_CONSUMER")
        put("enable.auto.commit", false)
        put("isolation.level", "read_committed")
        put("auto.offset.reset", "earliest")
    }

    val producerProperties = Properties().apply {
        put("bootstrap.servers", kafkaServers)
        put("group.id", "TICKER_PRODUCER")
        put("transactional.id", "TICKER_PRODUCER_TR_ID")
    }

    val cassandraProperties = Properties().apply {
        put("cassandraHost", env("CASSANDRA_HOSTS", "localhost"))
        put("cassandraPort", env("CASSANDRA_PORT", "9042"))
    }

    fun createTickerTopic() {
        val props = Properties()
        props.setProperty(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServers)

        val adminClient = AdminClient.create(props)

        val numPartitions = 1
        val replicationFactor = 1
        val newTopic = NewTopic(tickersTopicName, numPartitions, replicationFactor.toShort())
        val configs = mapOf(
                TopicConfig.RETENTION_MS_CONFIG to TimeUnit.DAYS.toMillis(1).toString(),
                TopicConfig.CLEANUP_POLICY_CONFIG to TopicConfig.CLEANUP_POLICY_DELETE
        )

        newTopic.configs(configs)
        adminClient.createTopics(listOf(newTopic))
    }

}