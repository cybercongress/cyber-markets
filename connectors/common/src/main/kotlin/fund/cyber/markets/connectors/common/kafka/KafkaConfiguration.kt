package fund.cyber.markets.connectors.common.kafka

import fund.cyber.markets.configuration.env
import java.util.*

class KafkaConfiguration(
        val kafkaServers: String = env("KAFKA_CONNECTION", "localhost:9092")
) {
    fun tradesConsumersProperties(groupId: String): Properties {
        return Properties().apply {
            put("bootstrap.servers", kafkaServers)
            put("group.id", groupId)
        }
    }
}
