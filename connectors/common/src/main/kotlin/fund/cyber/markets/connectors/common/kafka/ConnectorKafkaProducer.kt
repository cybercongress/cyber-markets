package fund.cyber.markets.connectors.common.kafka

import fund.cyber.markets.kafka.JsonSerializer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.common.serialization.StringSerializer
import java.util.*

class ConnectorKafkaProducer<T>(
        props: Properties = KafkaConfiguration().tradesConsumersProperties("3"),
        keySerializer: StringSerializer = StringSerializer(),
        valueSerializer: JsonSerializer<T> = JsonSerializer<T>()
): KafkaProducer<String, T>(props, keySerializer, valueSerializer)