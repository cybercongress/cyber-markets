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
private val numPartitions = 1
private val replicationFactor: Short = 1

class TickersConfiguration(
        val kafkaServers: String = env(Constants.KAFKA_CONNECTION, "localhost:9092"),
        val topicNamePattern: Pattern = tradesTopicNamePattern,
        val topicResubscribe: Long = TimeUnit.MINUTES.toMillis(1),
        val windowDurationsString: String = env(Constants.WINDOW_DURATIONS_MIN, "1,5,15,30,60,180,240,360,720,1440"),
        val windowHop: Long = TimeUnit.SECONDS.toMillis(env(Constants.WINDOW_HOP_SEC, 3)),
        val tickersTopicName: String = "TICKERS",
        val tickersBackupTopicName: String = "TICKERS-BACKUP",
        val allowNotClosedWindows: Boolean = env("ALLOW_NOT_CLOSED_WINDOWS", "true").toBoolean()
) {

    val windowDurations = windowDurationsString.split(",").map { it -> TimeUnit.MINUTES.toMillis(it.toLong()) }

    val consumerProperties = Properties().apply {
        put(ConsumerConfig.METADATA_MAX_AGE_CONFIG, topicResubscribe)
        put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 25000)
        put("bootstrap.servers", kafkaServers)
        put("group.id", "TRADE_CONSUMER")
        put("enable.auto.commit", false)
        put("isolation.level", "read_committed")
        put("auto.offset.reset", "earliest")
    }

    val consumerTickersBackupsProperties = Properties().apply {
        put("bootstrap.servers", kafkaServers)
        put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 25000)
        put("group.id", "TICKER_BACKUP_CONSUMER")
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
        val adminClient = AdminClient.create(Properties().apply {
            put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServers)
        })

        val tickersTopic = NewTopic(tickersTopicName, numPartitions, replicationFactor)
        val tickersBackupTopic = NewTopic(tickersBackupTopicName, numPartitions, replicationFactor)
        val configs = mapOf(
                TopicConfig.RETENTION_MS_CONFIG to TimeUnit.DAYS.toMillis(1).toString(),
                TopicConfig.CLEANUP_POLICY_CONFIG to TopicConfig.CLEANUP_POLICY_DELETE
        )

        tickersTopic.configs(configs)
        tickersBackupTopic.configs(configs)
        adminClient.createTopics(listOf(tickersTopic, tickersBackupTopic))
    }

}