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


@RestController
class ConnectorInfoController {

    @Autowired
    private lateinit var connectorService: ConnectorService

    @GetMapping("/exchanges")
    fun getExchanges(): Mono<ResponseEntity<List<String>>> {

        return connectorService.getExchanges()
            .collectList()
            .map { exchangeList -> ok().body(exchangeList) }
            .defaultIfEmpty(notFound().build()
        )
    }

    @GetMapping("/exchange/{exchangeName}/pairs")
    fun getPairs(
        @PathVariable exchangeName: String
    ): Mono<ResponseEntity<List<TokensPair>>> {

        return connectorService.getTokensPairsByExchange(exchangeName.toUpperCase())
            .collectList()
            .map { pairs -> ok().body(pairs) }
            .defaultIfEmpty(notFound().build())
    }

}