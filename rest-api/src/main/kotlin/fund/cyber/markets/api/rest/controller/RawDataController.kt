package fund.cyber.markets.api.rest.controller

import fund.cyber.markets.cassandra.model.CqlTokensPair
import fund.cyber.markets.cassandra.model.CqlTrade
import fund.cyber.markets.cassandra.repository.OrderBookRepository
import fund.cyber.markets.cassandra.repository.TradeRepository
import fund.cyber.markets.common.model.OrderBook
import fund.cyber.markets.common.model.TokensPair
import fund.cyber.markets.common.rest.service.ConnectorService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.notFound
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono


@RestController
class RawDataController {

    @Autowired
    private lateinit var connectorService: ConnectorService

    @Autowired
    private lateinit var orderBookRepository: OrderBookRepository

    @Autowired
    private lateinit var tradeRepository: TradeRepository

    @GetMapping("/orderbook")
    fun getOrderBook(
        @RequestParam exchange: String,
        @RequestParam pair: String,
        @RequestParam(required = false) ts: Long?
    ): Mono<ResponseEntity<OrderBook>> {

        val tokensPair = TokensPair(pair.toUpperCase())
        var orderBook: Mono<OrderBook> = Mono.empty()

        if (ts != null) {
            //todo:
            //orderBook = orderBookRepository.getNearest(exchange.toUpperCase(), CqlTokensPair(pair.toUpperCase()), ts)
        } else {
            orderBook = connectorService.getOrderBook(exchange.toUpperCase(), tokensPair)
        }

        return orderBook.map { orderBook ->
            ok().body(orderBook)
        }
            .defaultIfEmpty(notFound().build())

    }

    @GetMapping("/trade")
    fun getTrades(
        @RequestParam exchange: String,
        @RequestParam pair: String,
        @RequestParam epochMin: Long
    ): Mono<ResponseEntity<List<CqlTrade>>> {
        val trades = tradeRepository.get(exchange.toUpperCase(), CqlTokensPair(pair.toUpperCase()), epochMin)

        return if (trades != null && trades.isNotEmpty()) {
            ok().body(trades).toMono()
        } else {
            notFound().build<List<CqlTrade>>().toMono()
        }
    }

}