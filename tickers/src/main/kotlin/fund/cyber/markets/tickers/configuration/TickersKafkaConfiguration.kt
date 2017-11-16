package fund.cyber.markets.tickers.configuration

import fund.cyber.markets.common.Constants
import fund.cyber.markets.helpers.env
import org.apache.kafka.streams.StreamsConfig
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

val tradesTopicNamePattern = Pattern.compile("TRADES-.*")
val tickersTopicName = "TICKERS-TOPIC"

class KafkaConfiguration(
        val kafkaServers: String = env(Constants.KAFKA_CONNECTION, "localhost:9092"),
        val topicNamePattern: Pattern = tradesTopicNamePattern,
        val topicResubscribe: Long = TimeUnit.MINUTES.toMillis(1),
        val windowDurationsString: String = env(Constants.WINDOW_DURATIONS_MIN, "1,5,15,30,60,180,240,360,720,1440"),
        val windowHop: Long = env(Constants.WINDOW_HOP_SEC, 3000)
) {

    fun windowDurations() : List<Long> {
       return windowDurationsString.split(",").map { it -> it.toLong()*60*1000 }
    }

    fun tickerStreamProperties(): Properties {
        return Properties().apply {
            put(StreamsConfig.APPLICATION_ID_CONFIG, "tickers_stream_module")
            put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServers)
            put(StreamsConfig.METADATA_MAX_AGE_CONFIG, topicResubscribe)
        }
    }
}