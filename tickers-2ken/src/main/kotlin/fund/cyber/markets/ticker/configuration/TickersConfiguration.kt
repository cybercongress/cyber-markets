package fund.cyber.markets.ticker.configuration

import fund.cyber.markets.common.Durations
import fund.cyber.markets.configuration.env
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.AdminClientConfig
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.config.TopicConfig
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

private const val KAFKA_CONNECTION: String = "KAFKA_CONNECTION"
private const val WINDOW_DURATIONS_MIN: String = "WINDOW_DURATIONS_MIN"
private const val WINDOW_HOP_SEC: String = "WINDOW_HOP_SEC"
private const val ALLOW_NOT_CLOSED_WINDOWS: String = "ALLOW_NOT_CLOSED_WINDOWS"

private const val NUMBER_OF_PARTITIONS = 1
private const val REPLICATION_FACTOR: Short = 1
private const val MAX_POLL_RECORDS = 20000
private const val POLL_TIMEOUT_COEFFICIENT: Double = 0.5

@Component
class TickersConfiguration(
        private val kafkaServers: String = env(KAFKA_CONNECTION, "localhost:9092"),
        val topicNamePattern: Pattern = Pattern.compile("TRADES-.*"),
        val windowHop: Long = TimeUnit.SECONDS.toMillis(env(WINDOW_HOP_SEC, 3)),
        val pollTimeout: Long = (windowHop * POLL_TIMEOUT_COEFFICIENT).toLong(),
        val tickersTopicName: String = "TICKERS",
        val volumesTopicName: String = "VOLUMES",
        val tickersBackupTopicName: String = "TICKERS-BACKUP",
        val volumesBackupTopicName: String = "VOLUMES-BACKUP",
        val allowNotClosedWindows: Boolean = env(ALLOW_NOT_CLOSED_WINDOWS, "true").toBoolean()
) {

    val windowDurations: MutableSet<Long> = env(WINDOW_DURATIONS_MIN, "1,5,15,30,60,180,240,360,720,1440")
            .split(",")
            .map { it -> TimeUnit.MINUTES.toMillis(it.toLong()) }
            .toMutableSet()
            .apply {
                add(Durations.MINUTE)
                add(Durations.HOUR)
                add(Durations.DAY)
            }

    val tickerConsumerConfig = Properties().apply {
        put(ConsumerConfig.METADATA_MAX_AGE_CONFIG, Durations.MINUTE)
        put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, MAX_POLL_RECORDS)
        put("bootstrap.servers", kafkaServers)
        put("group.id", "TRADE_CONSUMER")
        put("enable.auto.commit", false)
        put("isolation.level", "read_committed")
        put("auto.offset.reset", "earliest")
    }

    val tickersBackupConsumerConfig = Properties().apply {
        put("bootstrap.servers", kafkaServers)
        put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, MAX_POLL_RECORDS)
        put("group.id", "TICKER_BACKUP_CONSUMER")
        put("enable.auto.commit", false)
        put("isolation.level", "read_committed")
        put("auto.offset.reset", "earliest")
    }

    val tickerProducerConfig = Properties().apply {
        put("bootstrap.servers", kafkaServers)
        put("group.id", "TICKER_PRODUCER")
        put("transactional.id", "TICKER_PRODUCER_TR_ID")
    }

    val volumeBackupConsumerConfig = Properties().apply {
        put("bootstrap.servers", kafkaServers)
        put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, MAX_POLL_RECORDS)
        put("group.id", "VOLUME_BACKUP_CONSUMER")
        put("enable.auto.commit", false)
        put("isolation.level", "read_committed")
        put("auto.offset.reset", "earliest")
    }

    val volumeProducerConfig = Properties().apply {
        put("bootstrap.servers", kafkaServers)
        put("group.id", "VOLUME_PRODUCER")
        put("transactional.id", "VOLUME_PRODUCER_TR_ID")
    }

    val cassandraProperties = Properties().apply {
        put("cassandraHost", env("CASSANDRA_HOSTS", "localhost"))
        put("cassandraPort", env("CASSANDRA_PORT", "9042"))
    }

    init {
        createTopics()
    }

    private fun createTopics() {
        val adminClient = AdminClient.create(Properties().apply {
            put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServers)
        })

        val tickersTopic = NewTopic(tickersTopicName, NUMBER_OF_PARTITIONS, REPLICATION_FACTOR)
        val tickersBackupTopic = NewTopic(tickersBackupTopicName, NUMBER_OF_PARTITIONS, REPLICATION_FACTOR)
        val volumesTopic = NewTopic(volumesTopicName, NUMBER_OF_PARTITIONS, REPLICATION_FACTOR)
        val volumesBackupTopic = NewTopic(volumesBackupTopicName, NUMBER_OF_PARTITIONS, REPLICATION_FACTOR)

        val configs = mapOf(
                TopicConfig.RETENTION_MS_CONFIG to TimeUnit.DAYS.toMillis(1).toString(),
                TopicConfig.CLEANUP_POLICY_CONFIG to TopicConfig.CLEANUP_POLICY_DELETE
        )

        tickersTopic.configs(configs)
        tickersBackupTopic.configs(configs)
        volumesTopic.configs(configs)
        volumesBackupTopic.configs(configs)

        adminClient.createTopics(listOf(tickersTopic, tickersBackupTopic, volumesTopic, volumesBackupTopic))
    }

}