package fund.cyber.markets.api.rest.controller

import fund.cyber.markets.cassandra.model.CqlOrderBook
import fund.cyber.markets.cassandra.model.CqlTokensPair
import fund.cyber.markets.cassandra.model.CqlTrade
import fund.cyber.markets.cassandra.repository.OrderBookRepository
import fund.cyber.markets.cassandra.repository.TradeRepository
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


const val ORDERBOOK_SNAPSHOT_PERIOD: Long = 10 * 60 * 1000

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
    ): Mono<ResponseEntity<CqlOrderBook>> {
        var orderBook: CqlOrderBook? = null

        if (ts != null) {
            orderBook = orderBookRepository.getNearest(exchange.toUpperCase(), CqlTokensPair(pair.toUpperCase()), ts)
        } else {
            val tokensPair = TokensPair(pair.toUpperCase())
            val currentOrderBook = connectorService.getOrderBook(exchange.toUpperCase(), tokensPair)
            if (currentOrderBook != null) {
                orderBook = CqlOrderBook(exchange, tokensPair, currentOrderBook)
            }
        }

        return if (orderBook != null) {
            ok().body(orderBook).toMono()
        } else {
            notFound().build<CqlOrderBook>().toMono()
        }
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