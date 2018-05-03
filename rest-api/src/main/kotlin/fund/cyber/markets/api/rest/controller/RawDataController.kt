package fund.cyber.markets.api.rest.controller

import fund.cyber.markets.api.rest.configuration.RestApiConfiguration
import fund.cyber.markets.common.model.OrderBook
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
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

@RestController
class RawDataController {

    @Autowired
    private lateinit var configuration: RestApiConfiguration

    private val restTemplate = RestTemplate()

    @GetMapping("/orderbook")
    fun getOrderBook(
            @RequestParam exchange: String,
            @RequestParam pair: String,
            @RequestParam(required = false) ts: Long?
            ): Mono<ResponseEntity<OrderBook>> {

        val requestUri = configuration.exchangesConnectorServiceUrl + ORDERBOOK_PATH

        val builder = UriComponentsBuilder.fromUriString(requestUri)
            .queryParam("exchange", exchange)
            .queryParam("pair", pair)

        val orderBook: OrderBook?
        try {
            orderBook = restTemplate.getForObject<OrderBook>(builder.toUriString(), OrderBook::class.java)
        } catch (e: HttpClientErrorException) {
            return Mono.just(ResponseEntity(HttpStatus.valueOf(e.rawStatusCode)))
        }

        return ok().body(orderBook).toMono()
    }

}