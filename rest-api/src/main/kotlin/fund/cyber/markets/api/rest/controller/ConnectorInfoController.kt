package fund.cyber.markets.api.rest.controller

import fund.cyber.markets.common.model.TokensPair
import fund.cyber.markets.common.rest.service.ConnectorService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.notFound
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono


@RestController
class ConnectorInfoController {

    @Autowired
    private lateinit var connectorService: ConnectorService

    @GetMapping("/exchanges")
    fun getExchanges(): Mono<ResponseEntity<Set<String>>> {

        val exchanges = connectorService.getExchanges()

        return if (exchanges != null) {
            ok().body(exchanges).toMono()
        } else {
            notFound().build<Set<String>>().toMono()
        }
    }

    @GetMapping("/exchange/{exchangeName}/pairs")
    fun getTrades(
        @PathVariable exchangeName: String
    ): Mono<ResponseEntity<Set<TokensPair>>> {
        val pairs = connectorService.getTokensPairsByExchange(exchangeName.toUpperCase())

        return if (pairs != null && pairs.isNotEmpty()) {
            ok().body(pairs).toMono()
        } else {
            notFound().build<Set<TokensPair>>().toMono()
        }
    }

}