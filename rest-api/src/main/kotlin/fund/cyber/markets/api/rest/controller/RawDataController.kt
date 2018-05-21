package fund.cyber.markets.api.rest.controller

import fund.cyber.markets.api.rest.common.toOrderBook
import fund.cyber.markets.cassandra.model.CqlTokensPair
import fund.cyber.markets.cassandra.model.CqlTrade
import fund.cyber.markets.cassandra.repository.OrderBookRepository
import fund.cyber.markets.cassandra.repository.TradeRepository
import fund.cyber.markets.common.MILLIS_TO_HOURS
import fund.cyber.markets.common.convert
import fund.cyber.markets.common.model.OrderBook
import fund.cyber.markets.common.model.TokensPair
import fund.cyber.markets.common.rest.service.ConnectorService
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.notFound
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import java.util.*


@RestController
class RawDataController(
    private val connectorService: ConnectorService,
    private val orderBookRepository: OrderBookRepository,
    private val tradeRepository: TradeRepository
) {

    @GetMapping("/orderbook")
    fun getOrderBook(
        @RequestParam exchange: String,
        @RequestParam pair: String,
        @RequestParam(required = false) ts: Long?
    ): ResponseEntity<OrderBook> {

        val tokensPair = TokensPair(pair.toUpperCase())
        val orderBook: OrderBook?

        if (ts != null) {
            orderBook = orderBookRepository
                .findLastByTimestamp(exchange.toUpperCase(), CqlTokensPair(tokensPair), ts convert MILLIS_TO_HOURS, Date(ts))
                .block()
                ?.toOrderBook()
        } else {
            orderBook = connectorService.getOrderBook(exchange.toUpperCase(), tokensPair).block()
        }

        return if (orderBook != null) {
            ok().body(orderBook)
        } else {
            notFound().build()
        }
    }

    @GetMapping("/trade")
    fun getTrades(
        @RequestParam exchange: String,
        @RequestParam pair: String,
        @RequestParam epochMin: Long
    ): ResponseEntity<Flux<CqlTrade>> {
        val trades = tradeRepository.find(exchange.toUpperCase(), CqlTokensPair(pair.toUpperCase()), epochMin)

        return ok().body(trades)
    }

}