package fund.cyber.markets.api.rest.handler

import com.fasterxml.jackson.databind.ObjectMapper
import fund.cyber.markets.common.model.Token
import fund.cyber.markets.common.model.TokensPair
import fund.cyber.markets.common.rest.service.ConnectorService
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.notFound
import org.springframework.web.reactive.function.server.ServerResponse.ok
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono

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
        val exchange = try {
            request.pathVariable("exchangeName").toUpperCase()
        } catch (e: IllegalArgumentException) {
            return ServerResponse.badRequest().build()
        }

        val pairs = connectorService.getTokensPairsByExchange(exchange)

        return ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(pairs, TokensPair::class.java)
            .switchIfEmpty(
                notFound().build()
            )
    }

    fun getTokens(request: ServerRequest): Mono<ServerResponse> {
        return ok()
            .body(connectorService.getTokens(), Token::class.java)
            .switchIfEmpty(
                notFound().build()
            )
    }

    fun getTokensByExchange(request: ServerRequest): Mono<ServerResponse> {
        val exchange = try {
            request.pathVariable("exchangeName").toUpperCase()
        } catch (e: IllegalArgumentException) {
            return ServerResponse.badRequest().build()
        }

        return ok()
            .body(connectorService.getTokensByExchange(exchange), Token::class.java)
            .switchIfEmpty(
                notFound().build()
            )
    }

    fun getTokensCount(request: ServerRequest): Mono<ServerResponse> {
        var count = 0L

        val counts = connectorService
            .getTokensCount()
            .collectList()
            .block()

        return ok()
            .body(count.toMono(), Long::class.java)
            .switchIfEmpty(
                notFound().build()
            )
    }

    fun getTokensCountByExchange(request: ServerRequest): Mono<ServerResponse> {
        val exchange = try {
            request.pathVariable("exchangeName").toUpperCase()
        } catch (e: IllegalArgumentException) {
            return ServerResponse.badRequest().build()
        }

        return ok()
            .body(connectorService.getTokensCountByExchange(exchange), Long::class.java)
            .switchIfEmpty(
                notFound().build()
            )
    }

}