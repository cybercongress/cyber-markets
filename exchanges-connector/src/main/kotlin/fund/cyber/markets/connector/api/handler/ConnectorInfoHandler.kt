package fund.cyber.markets.connector.api.handler

import fund.cyber.markets.common.model.StringWrapper
import fund.cyber.markets.common.model.Token
import fund.cyber.markets.common.model.TokensPair
import fund.cyber.markets.connector.ConnectorRunner
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.badRequest
import org.springframework.web.reactive.function.server.ServerResponse.notFound
import org.springframework.web.reactive.function.server.ServerResponse.ok
import reactor.core.publisher.Mono
import reactor.core.publisher.toFlux
import reactor.core.publisher.toMono

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
        val exchange = try {
            request.pathVariable("exchangeName").toUpperCase()
        } catch (e: IllegalArgumentException) {
            return badRequest().build()
        }

        val pairs = connectorRunner.exchangesConnectors()[exchange]?.getTokensPairs() ?: setOf()

        return ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(pairs.toFlux(), TokensPair::class.java)
            .switchIfEmpty(
                notFound().build()
            )
    }

    fun getPairsCountByExchange(request: ServerRequest): Mono<ServerResponse> {
        val exchange = try {
            request.pathVariable("exchangeName").toUpperCase()
        } catch (e: IllegalArgumentException) {
            return badRequest().build()
        }

        val pairsCountByExchange = (connectorRunner.exchangesConnectors()[exchange]?.getTokensPairs() ?: setOf()).size.toLong()

        return ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(pairsCountByExchange.toMono(), Long::class.java)
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

    fun getTokens(request: ServerRequest): Mono<ServerResponse> {
        val tokens = getTokensEntities()

        return ok()
            .body(tokens.toFlux(), Token::class.java)
            .switchIfEmpty(
                notFound().build()
            )
    }

    fun getTokensByExchange(request: ServerRequest): Mono<ServerResponse> {
        val exchange = try {
            request.pathVariable("exchangeName").toUpperCase()
        } catch (e: IllegalArgumentException) {
            return badRequest().build()
        }

        val tokensByExchange = connectorRunner.exchangesConnectors()[exchange]?.getTokens() ?: setOf()

        return ok()
            .body(tokensByExchange.toFlux(), Token::class.java)
            .switchIfEmpty(
                notFound().build()
            )
    }

    fun getTokensCountByExchange(request: ServerRequest): Mono<ServerResponse> {
        val exchange = try {
            request.pathVariable("exchangeName").toUpperCase()
        } catch (e: IllegalArgumentException) {
            return badRequest().build()
        }

        val tokensCountByExchange = (connectorRunner.exchangesConnectors()[exchange]?.getTokens() ?: setOf()).size.toLong()

        return ok()
            .body(tokensCountByExchange.toMono(), Long::class.java)
            .switchIfEmpty(
                notFound().build()
            )
    }

    fun getTokensCount(request: ServerRequest): Mono<ServerResponse> {
        val tokensCount = getTokensEntities().size.toLong()

        return ok()
            .body(tokensCount.toMono(), Long::class.java)
            .switchIfEmpty(
                notFound().build()
            )
    }

    private fun getTokensEntities(): Set<Token> {
        val tokens = mutableSetOf<Token>()

        exchanges.forEach { exchange ->
            val tokensByExchange = connectorRunner.exchangesConnectors()[exchange]?.getTokens() ?: setOf()

            tokensByExchange.forEach { token ->
                tokens.add(token)
            }
        }

        return tokens
    }

}