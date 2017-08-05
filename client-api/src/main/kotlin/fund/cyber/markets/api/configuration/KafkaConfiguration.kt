package fund.cyber.markets.api.configuration


import java.util.*
import java.util.regex.Pattern


class KafkaConfiguration(

        val kafkaServers: String = "localhost:9092",

        val tradesTopicNamePattern: Pattern = Pattern.compile("TRADES-.*"),
        val tradesPoolAwaitTimeout: Long = 300,
        val tradesTopicResubscribe: Long = 1 * 60 * 1000
) {

    fun tradesConsumersProperties(groupId: String): Properties {
        return Properties().apply {
            put("bootstrap.servers", kafkaServers)
            put("group.id", groupId)
            put("metadata.max.age.ms", tradesTopicResubscribe)
        }
    }
}