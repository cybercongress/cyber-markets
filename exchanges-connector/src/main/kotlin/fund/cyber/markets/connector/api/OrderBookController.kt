package fund.cyber.markets.connector.api

import fund.cyber.markets.common.model.OrderBook
import fund.cyber.markets.common.model.TokensPair
import fund.cyber.markets.connector.service.OrderBookService
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.notFound
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
class OrderBookController(
        private val orderBookService: OrderBookService
) {

    @GetMapping("/orderbook")
    fun getOrderBook(
        @RequestParam(required = true) exchange: String,
        @RequestParam(required = true) pair: String
    ): Mono<ResponseEntity<OrderBook>> {

        return orderBookService.getOrderBook(exchange.toUpperCase(), TokensPair(pair.toUpperCase()))
            .map { orderBook ->
                ok().body(orderBook)
            }
            .defaultIfEmpty(notFound().build())
    }

}