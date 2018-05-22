package fund.cyber.markets.connector.api.handler

import fund.cyber.markets.common.model.StringWrapper
import fund.cyber.markets.common.model.TokensPair
import fund.cyber.markets.connector.ConnectorRunner
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.notFound
import org.springframework.web.reactive.function.server.ServerResponse.ok
import reactor.core.publisher.Mono
import reactor.core.publisher.toFlux

@Component
class ConnectorInfoHandler(
    private val exchanges: Set<String>,
    private val connectorRunner: ConnectorRunner
) {

    fun getConnectedExchanges(request: ServerRequest): Mono<ServerResponse> {
        return ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(exchanges.map { exchangeName -> StringWrapper(exchangeName) }.toFlux(), StringWrapper::class.java)
    }

    fun getPairs(request: ServerRequest): Mono<ServerResponse> {
        val exchange = request.pathVariable("exchangeName").toUpperCase()
        val pairs = connectorRunner.exchangesConnectors()[exchange]?.getTokensPairs() ?: setOf()

        return ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(pairs.toFlux(), TokensPair::class.java)
            .switchIfEmpty(
                notFound().build()
            )
    }

    fun isAlive(request: ServerRequest): Mono<ServerResponse> {
        var response = ok().build()

        connectorRunner.exchangesConnectors().forEach { _, connector ->
            if (!connector.isAlive()) {
                response = ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE).build()
            }
        }

        return response
    }

}