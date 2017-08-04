package fund.cyber.markets

import com.fasterxml.jackson.databind.ObjectMapper
import fund.cyber.markets.api.common.RootWebSocketHandler
import fund.cyber.markets.kafka.JsonDeserializer
import fund.cyber.markets.model.TokensPair
import fund.cyber.markets.model.Trade
import io.undertow.Handlers
import io.undertow.Handlers.path
import io.undertow.Undertow
import kotlinx.coroutines.experimental.newSingleThreadContext
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.KafkaConsumer
import java.util.*
import org.apache.kafka.common.errors.WakeupException
import java.util.HashMap
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.internals.NoOpConsumerRebalanceListener
import java.util.regex.Pattern


val applicationSingleThreadContext = newSingleThreadContext("Coroutines Single Thread Pool")
val jsonParser = ObjectMapper()

val props: Properties = Properties().apply {
    put("bootstrap.servers", "localhost:9092")
    put("retries", 5)
    put("batch.size", 16384)
    put("buffer.memory", 33554432)
    put("metadata.max.age.ms", 1 * 60 * 1000)
    put("group.id", "trades")
}

val keyDeserializer = JsonDeserializer(TokensPair::class.java)
val valueDeserializer = JsonDeserializer(Trade::class.java)
val consumer: Consumer<TokensPair, Trade> = KafkaConsumer(props, keyDeserializer, valueDeserializer)


fun main(args: Array<String>) {

    val server = Undertow.builder()
            .addHttpListener(8082, "127.0.0.1")
            .setHandler(path()
                    .addPrefixPath("/", Handlers.websocket(RootWebSocketHandler()))
            )
            .build()
    server.start()


    consumer.use { consumer ->

        val pattern = Pattern.compile("TRADES-.*")
        consumer.subscribe(pattern, NoOpConsumerRebalanceListener())

        while (true) {
            val records = consumer.poll(500)
            for (record in records) {
                println(record.key())
                print("   -   ")
                print(record.value())
            }
        }
    }
}



