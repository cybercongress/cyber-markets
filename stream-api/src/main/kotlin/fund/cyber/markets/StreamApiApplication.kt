package fund.cyber.markets

import fund.cyber.markets.api.common.*
import fund.cyber.markets.model.Order
import fund.cyber.markets.model.Trade
import fund.cyber.markets.model.Ticker
import io.undertow.Handlers
import io.undertow.Handlers.path
import io.undertow.Undertow
import kotlinx.coroutines.experimental.newSingleThreadContext
import org.slf4j.LoggerFactory
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


val tradesSingleThreadContext = newSingleThreadContext("Coroutines Single Thread Pool For Trades")
val ordersSingleThreadContext = newSingleThreadContext("Coroutines Single Thread Pool For Orders")
val tickersSingleThreadContext = newSingleThreadContext("Coroutines Single Thread Pool For Tickers")

object StreamApiApplication {
    private val LOGGER = LoggerFactory.getLogger(StreamApiApplication::class.java)!!

    @JvmStatic
    fun main(args: Array<String>) {
        val tradesBroadcastersIndex = TradesBroadcastersIndex()
        val tradesChannelIndex = ChannelsIndex<Trade>()
        tradesChannelIndex.addChannelsListener(tradesBroadcastersIndex)

        val ordersBroadcastersIndex = OrdersBroadcastersIndex()
        val ordersChannelIndex = ChannelsIndex<List<Order>>()
        ordersChannelIndex.addChannelsListener(ordersBroadcastersIndex)

        val tickersBroadcastersIndex = TickersBroadcastersIndex()
        val tickersChannelIndex = ChannelsIndex<Ticker>()
        tickersChannelIndex.addChannelsListener(tickersBroadcastersIndex)

        val consumers = listOf(OrdersBatchConsumer(ordersChannelIndex), TradesConsumer(tradesChannelIndex), TickersConsumer(tickersChannelIndex))

        val messageHandler = IncomingMessagesHandler(tradesBroadcastersIndex, ordersBroadcastersIndex, tickersBroadcastersIndex)
        val rootWebSocketHandler = RootWebSocketHandler(messageHandler)

        val server = Undertow.builder()
                .addHttpListener(18080, "0.0.0.0")
                .setHandler(path()
                        .addPrefixPath("/", Handlers.websocket(rootWebSocketHandler))
                )
                .build()
        server.start()

        val executor = Executors.newFixedThreadPool(consumers.size)
        consumers.forEach { consumer ->
            executor.submit(consumer)
        }

        Runtime.getRuntime().addShutdownHook(object: Thread() {
            override fun run() {
                consumers.forEach {
                    it.shutdown()
                }
                executor.shutdown()
                try {
                    executor.awaitTermination(5000 , TimeUnit.MILLISECONDS)
                } catch (e: InterruptedException) {
                    LOGGER.error(e.message, e)
                }
            }
        })
    }
}




