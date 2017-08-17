package fund.cyber.markets.api.configuration

import fund.cyber.markets.helpers.env
import java.util.Properties
import java.util.concurrent.TimeUnit.MINUTES
import java.util.regex.Pattern

class KafkaConfiguration(
    val kafkaServers: String = env("KAFKA_CONNECTION", "localhost:9092"),
    val tradesTopicNamePattern: Pattern = Pattern.compile("TRADES-.*"),
    val tradesPoolAwaitTimeout: Long = 300,
    val tradesTopicResubscribe: Long = MINUTES.toMillis(1)
) {
    fun tradesConsumersProperties(groupId: String): Properties {
        return Properties().apply {
            put("bootstrap.servers", kafkaServers)
            put("group.id", groupId)
            put("metadata.max.age.ms", tradesTopicResubscribe)
        }
    }
}