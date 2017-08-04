package fund.cyber.markets.connectors.writer

import fund.cyber.markets.connectors.common.kafka.ConnectorKafkaProducer
import fund.cyber.markets.model.Trade
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord

/**
 * Created by aalbov on 4.8.17.
 */
interface MessageWriter {
    fun writeTradeMessage(trade: Trade)
}

class KafkaMessageWriter(
        private val producer: KafkaProducer<String, Trade> = ConnectorKafkaProducer<Trade>()
): MessageWriter {
    val tradeTopicPrefix = "TRADES"

    override fun writeTradeMessage(trade: Trade) {
        producer.send(ProducerRecord("$tradeTopicPrefix-${trade.exchange}", trade))
    }
}

class ConsoleMessageWriter(): MessageWriter {
    override fun writeTradeMessage(trade: Trade) {
        println(trade)
    }
}