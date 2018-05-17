package fund.cyber.markets.connector.api

import fund.cyber.markets.common.model.TokensPair
import fund.cyber.markets.connector.ConnectorRunner
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
    private lateinit var exchanges: Set<String>

    @Autowired
    private lateinit var connectorRunner: ConnectorRunner

    @GetMapping("/exchanges")
    fun getExchanges(): Mono<ResponseEntity<Set<String>>> {
        return ok().body(exchanges).toMono()
    }

    @GetMapping("/exchange/{exchangeName}/pairs")
    fun getPairsForExchange(
        @PathVariable exchangeName: String
    ): Mono<ResponseEntity<Set<TokensPair>>> {
        val pairs = connectorRunner.exchangesConnectors()[exchangeName.toUpperCase()]?.getTokensPairs()

        return if (pairs != null) {
            ok().body(pairs).toMono()
        } else {
            notFound().build<Set<TokensPair>>().toMono()
        }
    }

}