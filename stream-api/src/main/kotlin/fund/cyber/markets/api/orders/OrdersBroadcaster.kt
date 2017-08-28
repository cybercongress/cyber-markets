package fund.cyber.markets.api.orders

import com.fasterxml.jackson.databind.ObjectMapper
import fund.cyber.markets.api.common.Broadcaster
import fund.cyber.markets.api.configuration.AppContext
import fund.cyber.markets.model.Order
import fund.cyber.markets.ordersSingleThreadContext
import io.undertow.websockets.core.WebSocketChannel
import io.undertow.websockets.core.WebSockets
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.launch
import java.math.BigDecimal

typealias OrdersChannel = Channel<List<Order>>

class OrdersBroadcaster(
        private val newOrdersChannel: OrdersChannel,
        private val jsonSerializer: ObjectMapper = AppContext.jsonSerializer
) : Broadcaster {

    private val orderBook = mutableMapOf<BigDecimal, Order>()
    private val registeredChannels = HashSet<WebSocketChannel>()

    init {
        launch(ordersSingleThreadContext) {
            for (orders in newOrdersChannel) {
                handleNewOrders(orders)
            }
        }
    }

    private fun handleNewOrders(orders: List<Order>) {
        updateOrderBook(orders)
        if (registeredChannels.size == 0) {
            return
        }

        val ordersAsJson = jsonSerializer.writeValueAsString(orders)
        launch(ordersSingleThreadContext) {
            for (channel in registeredChannels) {
                WebSockets.sendText(ordersAsJson, channel, null)
            }
        }
    }

    private fun updateOrderBook(orders: List<Order>) {
        orders.forEach { newOrder ->
            orderBook.merge(newOrder.spotPrice, newOrder) { _, newOrder ->
                if (newOrder.amount == BigDecimal.ZERO) null else newOrder
            }
        }
    }


    override fun registerChannel(channel: WebSocketChannel) {
        launch(ordersSingleThreadContext) {
            val orderBookAsJson = jsonSerializer.writeValueAsString(orderBook.values)
            WebSockets.sendText(orderBookAsJson, channel, null)
            registeredChannels.add(channel)
        }
    }

    override fun unregisterChannel(channel: WebSocketChannel) {
        launch(ordersSingleThreadContext) {
            registeredChannels.remove(channel)
        }
    }
}
