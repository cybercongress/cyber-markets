package fund.cyber.markets.connector.api.handler

import fund.cyber.markets.common.model.OrderBook
import fund.cyber.markets.common.model.TokensPair
import fund.cyber.markets.connector.service.OrderBookService
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.notFound
import org.springframework.web.reactive.function.server.ServerResponse.ok
import reactor.core.publisher.Mono

@Component
class OrderBookHandler(
    private val orderBookService: OrderBookService
) {

    fun getOrderBook(request: ServerRequest): Mono<ServerResponse> {
        val exchange = request.queryParam("exchange").get().toUpperCase()
        val pair = request.queryParam("pair").get().toUpperCase()

        return ok()
            .body(orderBookService.getOrderBook(exchange, TokensPair(pair)), OrderBook::class.java)
            .switchIfEmpty(
                notFound().build()
            )
    }

}