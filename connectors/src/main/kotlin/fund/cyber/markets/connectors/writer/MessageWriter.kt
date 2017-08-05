package fund.cyber.markets.connectors.writer

import fund.cyber.markets.connectors.common.kafka.ConnectorKafkaProducer
import fund.cyber.markets.model.HasTopic
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord

/**
 * Created by aalbov on 4.8.17.
 */
interface MessageWriter<T> {
    fun writeMessage(message: T)
}

class KafkaMessageWriter<T: HasTopic>(
        private val producer: KafkaProducer<String, T> = ConnectorKafkaProducer<T>()
): MessageWriter<T> {
    override fun writeMessage(message: T) {
        producer.send(ProducerRecord(message.topic(), message))
    }
}

class ConsoleMessageWriter<T>(): MessageWriter<T> {
    override fun writeMessage(message: T) {
        println(message)
    }
}