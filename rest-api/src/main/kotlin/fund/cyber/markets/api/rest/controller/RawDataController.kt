package fund.cyber.markets.api.rest.controller

import fund.cyber.markets.cassandra.model.CqlOrderBook
import fund.cyber.markets.cassandra.repository.OrderBookRepository
import fund.cyber.markets.common.MILLIS_TO_HOURS
import fund.cyber.markets.common.closestSmallerMultiply
import fund.cyber.markets.common.convert
import fund.cyber.markets.common.model.OrderBook
import fund.cyber.markets.common.model.TokensPair
import fund.cyber.markets.common.rest.service.ConnectorService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.notFound
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono


const val ORDERBOOK_PATH = "/orderbook"
const val ORDERBOOK_SNAPSHOT_PERIOD: Long = 10 * 60 * 1000

@RestController
class RawDataController {

    @Autowired
    private lateinit var connectorService: ConnectorService

    @Autowired
    private lateinit var orderBookRepository: OrderBookRepository

    private val restTemplate = RestTemplate()

    @GetMapping("/orderbook")
    fun getOrderBook(
        @RequestParam exchange: String,
        @RequestParam pair: String,
        @RequestParam(required = false) ts: Long?
    ): Mono<ResponseEntity<CqlOrderBook>> {
        var orderBook: CqlOrderBook? = null

        if (ts != null) {
            val nearestTs = nearestOrderBookTimestamp(ts)
            val epochHour = nearestTs convert MILLIS_TO_HOURS
            orderBook = orderBookRepository.getNearlest(exchange.toUpperCase(), TokensPair(pair), epochHour, nearestTs)
        } else {
            val requestUri = connectorService.connectorsMap[exchange.toUpperCase()] + ORDERBOOK_PATH

            val builder = UriComponentsBuilder.fromUriString(requestUri)
                .queryParam("exchange", exchange.toUpperCase())
                .queryParam("pair", pair)

            try {
                val orderBookRest = restTemplate.getForObject<OrderBook>(builder.toUriString(), OrderBook::class.java)
                if (orderBookRest != null) {
                    orderBook = CqlOrderBook(exchange.toUpperCase(), TokensPair(pair), orderBookRest)
                }
            } catch (e: HttpClientErrorException) {
                return Mono.just(ResponseEntity(HttpStatus.valueOf(e.rawStatusCode)))
            }
        }

        return if (orderBook != null) {
            ok().body(orderBook).toMono()
        } else {
            notFound().build<CqlOrderBook>().toMono()
        }
    }

    private fun nearestOrderBookTimestamp(ts: Long): Long {
        return closestSmallerMultiply(ts, ORDERBOOK_SNAPSHOT_PERIOD)
    }

}