package fund.cyber.markets

import fund.cyber.markets.exchanges.bitfinex.BitfinexExchange
import fund.cyber.markets.exchanges.poloniex.PoloniexExchange
import io.undertow.protocols.ssl.UndertowXnioSsl
import io.undertow.server.DefaultByteBufferPool
import kotlinx.coroutines.experimental.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.experimental.Unconfined
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.newFixedThreadPoolContext
import kotlinx.coroutines.experimental.newSingleThreadContext
import org.xnio.OptionMap
import org.xnio.Options
import org.xnio.Xnio
import org.xnio.XnioWorker


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
val coroutinesThreads = 2
val coroutinesContext = newFixedThreadPoolContext(coroutinesThreads, "Coroutines Concurrent Pool")
val singleThreadContext = newSingleThreadContext("Coroutines Single Thread Pool")
/*----------------------------------------------------------------------------------*/

val supportedExchanges = listOf(
        PoloniexExchange(), BitfinexExchange()
)


fun main(args: Array<String>) {

    supportedExchanges.forEach { exchange ->
        async(Unconfined, UNDISPATCHED) {

            val dataChannel = exchange.subscribeData()
            async(coroutinesContext) {
                while (true) {
                    val tradesAndOrdersUpdatesMessage = dataChannel.receive()
                    println(tradesAndOrdersUpdatesMessage.trades)
                }
            }
        }
    }
}

