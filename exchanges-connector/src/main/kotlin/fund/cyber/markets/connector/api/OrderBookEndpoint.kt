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
class OrderBookEndpoint(
        private val orderBookService: OrderBookService
) {

    @GetMapping("/orderbook")
    fun getOrderBook(
        @RequestParam(value = "exchange", required = true) exchange: String,
        @RequestParam(value = "pair", required = true) tokensPair: String
    ): Mono<ResponseEntity<OrderBook>> {

        val pair = TokensPair(tokensPair.substringBefore("_"), tokensPair.substringAfter("_"))

        return orderBookService.getOrderBook(exchange, pair)
            .map { orderBook ->
                ok().body(orderBook)
            }
            .defaultIfEmpty(notFound().build())
    }

}