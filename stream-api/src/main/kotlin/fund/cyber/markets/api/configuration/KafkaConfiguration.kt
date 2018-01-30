package fund.cyber.markets.api.configuration

import fund.cyber.markets.helpers.env
import java.util.*
import java.util.concurrent.TimeUnit.MINUTES
import java.util.regex.Pattern

val tradesTopicNamePattern = Pattern.compile("TRADES-.*")
val ordersTopicNamePattern = Pattern.compile("ORDERS-.*")
val tickersTopicNamePattern = Pattern.compile("TICKERS")

class KafkaConfiguration(
        val kafkaServers: String = env("KAFKA_CONNECTION", "localhost:9092"),
        val topicNamePattern: Pattern = tradesTopicNamePattern,
        val poolAwaitTimeout: Long = 300,
        val topicResubscribe: Long = MINUTES.toMillis(1)
) {
    fun consumersProperties(groupId: String): Properties {
        return Properties().apply {
            put("bootstrap.servers", kafkaServers)
            put("group.id", groupId)
            put("metadata.max.age.ms", topicResubscribe)
            put("max.request.size", 126358200)
        }
    }
}