package fund.cyber.markets.connectors.common.kafka

import fund.cyber.markets.helpers.env
import java.util.Properties

class KafkaConfiguration(
        val kafkaServers: String = env("KAFKA_CONNECTION", "localhost:9092")
) {
    fun tradesConsumersProperties(groupId: String): Properties {
        return Properties().apply {
            put("bootstrap.servers", kafkaServers)
            put("max.request.size", 126358200)
            put("group.id", groupId)
        }
    }
}
