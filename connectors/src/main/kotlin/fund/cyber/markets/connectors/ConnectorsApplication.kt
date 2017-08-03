package fund.cyber.markets.connectors

import com.fasterxml.jackson.databind.ObjectMapper
import fund.cyber.markets.connectors.bitfinex.BitfinexExchange
import fund.cyber.markets.connectors.bitstamp.BitstampExchange
import fund.cyber.markets.connectors.helpers.concurrent
import fund.cyber.markets.connectors.hitbtc.HitBtcExchange
import fund.cyber.markets.connectors.poloniex.PoloniexExchange
import io.undertow.protocols.ssl.UndertowXnioSsl
import io.undertow.server.DefaultByteBufferPool
import kotlinx.coroutines.experimental.newFixedThreadPoolContext
import kotlinx.coroutines.experimental.newSingleThreadContext
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.xnio.OptionMap
import org.xnio.Options
import org.xnio.Xnio
import org.xnio.XnioWorker
import java.util.*


/*----------------------------- Undertow ------------------------------------------*/
val NO_OPTIONS: OptionMap = OptionMap.EMPTY

private val xnioSettings = OptionMap.builder()
        .set(Options.TCP_NODELAY, true)
        .set(Options.WORKER_IO_THREADS, 2)
        .set(Options.WORKER_TASK_CORE_THREADS, 5)
        .set(Options.WORKER_TASK_MAX_THREADS, 5)
        .set(Options.READ_TIMEOUT, 10 * 1000)
        .set(Options.WRITE_TIMEOUT, 10 * 1000)
        .getMap()

val xnioWorker: XnioWorker = Xnio.getInstance().createWorker(xnioSettings)
val xnioSsl = UndertowXnioSsl(Xnio.getInstance(), OptionMap.EMPTY)
val byteBuffersPool = DefaultByteBufferPool(true, 2048)
/*----------------------------------------------------------------------------------*/


/*----------------------------- Others ---------------------------------------------*/
val httpDispatcher = Dispatcher(xnioWorker)
val httpClient = OkHttpClient.Builder().dispatcher(httpDispatcher).build()!!

val coroutinesThreads = 2
val applicationContext = newFixedThreadPoolContext(coroutinesThreads, "Coroutines Concurrent Pool")
val applicationSingleThreadContext = newSingleThreadContext("Coroutines Single Thread Pool")

val jsonParser = ObjectMapper()
/*----------------------------------------------------------------------------------*/


val supportedExchanges = listOf(
        PoloniexExchange(), BitfinexExchange(), HitBtcExchange(), BitstampExchange()
)

// create instance for properties to access producer configs
val props: Properties = Properties().apply {
    //Assign localhost id
    put("bootstrap.servers", "localhost:9092");

    //Set acknowledgements for producer requests.      
    put("acks", "all");

    //If the request fails, the producer can automatically retry,
    put("retries", 0);

    //Specify buffer size in config
    put("batch.size", 16384);

    //Reduce the no of requests less than 0   
    put("linger.ms", 1);

    //The buffer.memory controls the total amount of memory available to the producer for buffering.   
    put("buffer.memory", 33554432);

    put("key.serializer",
            "org.apache.kafka.common.serialization.StringSerializer");

    put("value.serializer",
            "org.apache.kafka.common.serialization.StringSerializer");
}

val producer: Producer<String, String> = KafkaProducer(props)

fun main(args: Array<String>) {

    supportedExchanges.forEach { exchange ->
        concurrent {
            val dataChannel = exchange.subscribeData()
            concurrent {
                while (true) {
                    val tradesAndOrdersUpdatesMessage = dataChannel.receive()
                    tradesAndOrdersUpdatesMessage.trades.forEach { trade ->
                        producer.send(ProducerRecord(exchange.name, trade.toString()))
                    }
                    println(tradesAndOrdersUpdatesMessage.trades)
                }
            }
        }
    }
}

