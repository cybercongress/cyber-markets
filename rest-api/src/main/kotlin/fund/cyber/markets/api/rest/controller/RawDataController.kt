package fund.cyber.markets.api.rest.controller

import fund.cyber.markets.api.rest.configuration.RestApiConfiguration
import fund.cyber.markets.common.model.OrderBook
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Mono


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
            ): Mono<OrderBook> {

        val requestUri = configuration.exchangesConnectorServiceUrl + ORDERBOOK_PATH

        val builder = UriComponentsBuilder.fromUriString(requestUri)
            .queryParam("exchange", exchange)
            .queryParam("pair", pair)

        var response: OrderBook? = null
        try {
            response = restTemplate.getForObject<OrderBook>(builder.toUriString(), OrderBook::class.java)
        } catch (e: HttpClientErrorException) {

        }

        return Mono.just(response!!)
    }

}