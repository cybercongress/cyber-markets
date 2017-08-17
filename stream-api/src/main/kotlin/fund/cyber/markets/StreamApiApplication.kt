package fund.cyber.markets

import fund.cyber.markets.api.common.IncomingMessagesHandler
import fund.cyber.markets.api.common.RootWebSocketHandler
import fund.cyber.markets.api.configuration.KafkaConfiguration
import fund.cyber.markets.api.trades.TradesBroadcastersIndex
import fund.cyber.markets.api.trades.TradesChannelsIndex
import fund.cyber.markets.kafka.JsonDeserializer
import fund.cyber.markets.model.TokensPair
import fund.cyber.markets.model.Trade
import io.undertow.Handlers
import io.undertow.Handlers.path
import io.undertow.Undertow
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.newSingleThreadContext
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.consumer.internals.NoOpConsumerRebalanceListener
import org.apache.kafka.common.serialization.StringDeserializer


val applicationSingleThreadContext = newSingleThreadContext("Coroutines Single Thread Pool")

fun main(args: Array<String>) {

    val broadcastersIndex = TradesBroadcastersIndex()
    val tradesChannelIndex = TradesChannelsIndex()
    tradesChannelIndex.addTradesChannelsListener(broadcastersIndex)

    val messageHandler = IncomingMessagesHandler(broadcastersIndex)
    val rootWebSocketHandler = RootWebSocketHandler(messageHandler)

    val server = Undertow.builder()
            .addHttpListener(8082, "0.0.0.0")
            .setHandler(path()
                    .addPrefixPath("/", Handlers.websocket(rootWebSocketHandler))
            )
            .build()
    server.start()

    initializeTradesKafkaConsumers(tradesChannelIndex)
}


private fun initializeTradesKafkaConsumers(tradesChannelsIndex: TradesChannelsIndex) {

    val configuration = KafkaConfiguration()

    //there is no key in trades topics -> faked key deserializer
    val tradesDeserializer = JsonDeserializer(Trade::class.java)
    val keyDeserializer = StringDeserializer()
    val consumerProperties = configuration.tradesConsumersProperties("trades-1")


    KafkaConsumer(consumerProperties, keyDeserializer, tradesDeserializer).use { consumer ->
        consumer.subscribe(configuration.tradesTopicNamePattern, NoOpConsumerRebalanceListener())
        while (true) {
            handleNewTrades(tradesChannelsIndex, consumer.poll(configuration.tradesPoolAwaitTimeout))
        }
    }
}

private fun handleNewTrades(tradesChannelsIndex: TradesChannelsIndex, records: ConsumerRecords<String, Trade>) {
    launch(applicationSingleThreadContext) {
        records.map { record -> record.value() }
                .forEach { trade ->
                    val pair = TokensPair(trade.baseToken, trade.quoteToken)
                    tradesChannelsIndex.channelFor(trade.exchange, pair).send(trade)
                }
    }
}



