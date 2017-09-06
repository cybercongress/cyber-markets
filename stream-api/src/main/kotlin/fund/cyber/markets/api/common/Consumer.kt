package fund.cyber.markets.api.common

import fund.cyber.markets.api.configuration.KafkaConfiguration
import fund.cyber.markets.api.configuration.ordersTopicNamePattern
import fund.cyber.markets.kafka.JsonDeserializer
import fund.cyber.markets.model.Order
import fund.cyber.markets.model.OrdersBatch
import fund.cyber.markets.model.TokensPair
import fund.cyber.markets.model.Trade
import fund.cyber.markets.ordersSingleThreadContext
import fund.cyber.markets.tradesSingleThreadContext
import kotlinx.coroutines.experimental.launch
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.consumer.internals.NoOpConsumerRebalanceListener
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.StringDeserializer

abstract class Consumer<T>(
        val configuration: KafkaConfiguration = KafkaConfiguration(),
        keyDeserializer: Deserializer<String> = StringDeserializer(),
        valueDeserializer: Deserializer<T>,
        groupId: String
) : Runnable {
    val kafkaConsumer = KafkaConsumer(configuration.consumersProperties(groupId), keyDeserializer, valueDeserializer)

    override fun run() {
        kafkaConsumer.use {
            it.subscribe(configuration.topicNamePattern, NoOpConsumerRebalanceListener())
            while (true) {
                handleNew(it.poll(configuration.poolAwaitTimeout))
            }
        }
    }

    abstract fun handleNew(records: ConsumerRecords<String, T>)

    fun shutdown() {
        kafkaConsumer.wakeup()
    }
}

class OrdersBatchConsumer(
        val channelsIndex: ChannelsIndex<List<Order>>
) : Consumer<OrdersBatch>(
        configuration = KafkaConfiguration(topicNamePattern = ordersTopicNamePattern),
        groupId = "orders-1",
        valueDeserializer = JsonDeserializer(OrdersBatch::class.java)
) {

    override fun handleNew(records: ConsumerRecords<String, OrdersBatch>) {
        launch(ordersSingleThreadContext) {
            records.map { record -> record.value() }
                    .forEach { ordersBatch ->
                        val pair = TokensPair(ordersBatch.baseToken, ordersBatch.quoteToken)
                        channelsIndex.channelFor(ordersBatch.exchange, pair).send(ordersBatch.orders)
                    }
        }
    }
}

class TradesConsumer(
        val channelsIndex: ChannelsIndex<Trade>
) : Consumer<Trade>(
        groupId = "trades-1",
        valueDeserializer = JsonDeserializer(Trade::class.java)
) {

    override fun handleNew(records: ConsumerRecords<String, Trade>) {
        launch(tradesSingleThreadContext) {
            records.map { record -> record.value() }
                    .forEach { trade ->
                        val pair = TokensPair(trade.baseToken, trade.quoteToken)
                        channelsIndex.channelFor(trade.exchange, pair).send(trade)
                    }
        }
    }
}