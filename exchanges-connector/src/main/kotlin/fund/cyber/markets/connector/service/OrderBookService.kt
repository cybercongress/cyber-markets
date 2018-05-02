package fund.cyber.markets.connector.service

import fund.cyber.markets.common.model.OrderBook
import fund.cyber.markets.common.model.TokensPair
import fund.cyber.markets.connector.ConnectorRunner
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono

@Service
class OrderBookService(
    private val connectorRunner: ConnectorRunner
) {
    val connectors by lazy { connectorRunner.orderbookConnectors }

    fun getOrderBook(exchange: String, pair: TokensPair): Mono<OrderBook> {
        val orderbook = connectors[exchange]?.getOrderBookSnapshot(pair)

        return if (orderbook == null) {
            Mono.empty()
        } else {
            orderbook.toMono()
        }
    }
}