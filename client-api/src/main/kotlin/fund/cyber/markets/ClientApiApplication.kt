package fund.cyber.markets

import com.fasterxml.jackson.databind.ObjectMapper
import fund.cyber.markets.api.common.RootWebSocketHandler
import fund.cyber.markets.api.configuration.KafkaConfiguration
import fund.cyber.markets.kafka.JsonDeserializer
import fund.cyber.markets.model.Trade
import io.undertow.Handlers
import io.undertow.Handlers.path
import io.undertow.Undertow
import kotlinx.coroutines.experimental.newSingleThreadContext
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.consumer.internals.NoOpConsumerRebalanceListener
import org.apache.kafka.common.serialization.StringDeserializer


val applicationSingleThreadContext = newSingleThreadContext("Coroutines Single Thread Pool")
val jsonParser = ObjectMapper()


fun main(args: Array<String>) {

    val server = Undertow.builder()
            .addHttpListener(8082, "127.0.0.1")
            .setHandler(path()
                    .addPrefixPath("/", Handlers.websocket(RootWebSocketHandler()))
            )
            .build()
    server.start()
    initializeTradesKafkaConsumers()
}


private fun initializeTradesKafkaConsumers() {

    val configuration = KafkaConfiguration()

    //there is no key, in trades topics -> faked key deserializer
    val tradesDeserializer = JsonDeserializer(Trade::class.java)
    val keyDeserializer = StringDeserializer()
    val consumerProperties = configuration.tradesConsumersProperties("trades-1")
    val tradesConsumer: Consumer<String, Trade> = KafkaConsumer(consumerProperties, keyDeserializer, tradesDeserializer)

    tradesConsumer.use { consumer ->

        consumer.subscribe(configuration.tradesTopicNamePattern, NoOpConsumerRebalanceListener())
        while (true) {
            val records = consumer.poll(configuration.tradesPoolAwaitTimeout)
            for (record in records) {
                println(record.value())
            }
        }
    }
}



