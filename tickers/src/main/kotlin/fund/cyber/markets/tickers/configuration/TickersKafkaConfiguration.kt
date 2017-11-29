package fund.cyber.markets.tickers.configuration

import fund.cyber.markets.common.Constants
import fund.cyber.markets.helpers.env
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.AdminClientConfig
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.common.config.TopicConfig
import org.apache.kafka.streams.StreamsConfig
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

val tradesTopicNamePattern = Pattern.compile("TRADES-.*")
val tickersTopicName = "TICKERS"

class KafkaConfiguration(
        val kafkaServers: String = env(Constants.KAFKA_CONNECTION, "localhost:9092"),
        val topicNamePattern: Pattern = tradesTopicNamePattern,
        val topicResubscribe: Long = TimeUnit.MINUTES.toMillis(1),
        val windowDurationsString: String = env(Constants.WINDOW_DURATIONS_MIN, "1,5,15,30,60,180,240,360,720,1440"),
        val windowHop: Long = TimeUnit.SECONDS.toMillis(env(Constants.WINDOW_HOP_SEC, 3)),
        val streamCacheSizeMb: Long = env(Constants.CACHE_MAX_SIZE_MB, 200)
) {

    fun getWindowDurations(): List<Long> {
        return windowDurationsString.split(",").map { it -> it.toLong() * 60 * 1000 }
    }

    fun tickerStreamProperties(): Properties {
        return Properties().apply {
            put(StreamsConfig.APPLICATION_ID_CONFIG, "tickers_module")
            put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServers)
            put(StreamsConfig.METADATA_MAX_AGE_CONFIG, topicResubscribe)
            put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, streamCacheSizeMb * 1024 * 1024L)
            put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, windowHop)
        }
    }
}

fun createTickerTopic(kafkaServers: String) {
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