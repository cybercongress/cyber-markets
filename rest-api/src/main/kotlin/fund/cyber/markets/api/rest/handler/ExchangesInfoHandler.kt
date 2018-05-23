package fund.cyber.markets.api.rest.handler

import com.fasterxml.jackson.databind.ObjectMapper
import fund.cyber.markets.common.model.TokensPair
import fund.cyber.markets.common.rest.service.ConnectorService
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.notFound
import org.springframework.web.reactive.function.server.ServerResponse.ok
import reactor.core.publisher.Mono

@Component
class ExchangesInfoHandler(
    private val connectorService: ConnectorService
) {

    //todo: find a better way to serialize list of strings
    private val mapper = ObjectMapper()

    fun getConnectedExchanges(request: ServerRequest): Mono<ServerResponse> {
        return ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(
                connectorService
                    .getExchanges()
                    .collectList()
                    .map { list -> mapper.writeValueAsString(list) }, String::class.java
            )
    }

    fun getPairs(request: ServerRequest): Mono<ServerResponse> {
        val exchange = request.pathVariable("exchangeName").toUpperCase()
        val pairs = connectorService.getTokensPairsByExchange(exchange)

        return ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(pairs, TokensPair::class.java)
            .switchIfEmpty(
                notFound().build()
            )
    }

}