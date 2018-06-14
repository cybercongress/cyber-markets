package fund.cyber.markets.connector.api.handler

import fund.cyber.markets.common.model.StringWrapper
import fund.cyber.markets.common.model.Token
import fund.cyber.markets.common.rest.asServerResponse
import fund.cyber.markets.connector.ConnectorRunner
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.badRequest
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
        return exchanges.map { exchangeName -> StringWrapper(exchangeName) }.toFlux().asServerResponse()
    }

    fun getPairs(request: ServerRequest): Mono<ServerResponse> {
        val exchange = try {
            request.pathVariable("exchangeName").toUpperCase()
        } catch (e: IllegalArgumentException) {
            return badRequest().build()
        }

        val pairs = connectorRunner.exchangesConnectors()[exchange]?.getTokensPairs() ?: setOf()

        return pairs.toFlux().asServerResponse()
    }

    fun getPairsCountByExchange(request: ServerRequest): Mono<ServerResponse> {
        val exchange = try {
            request.pathVariable("exchangeName").toUpperCase()
        } catch (e: IllegalArgumentException) {
            return badRequest().build()
        }

        val pairsCountByExchange = (connectorRunner.exchangesConnectors()[exchange]?.getTokensPairs() ?: setOf()).size.toLong()

        return pairsCountByExchange.toMono().asServerResponse()
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

        return getTokensEntities().toFlux().asServerResponse()
    }

    fun getTokensByExchange(request: ServerRequest): Mono<ServerResponse> {
        val exchange = try {
            request.pathVariable("exchangeName").toUpperCase()
        } catch (e: IllegalArgumentException) {
            return badRequest().build()
        }

        val tokensByExchange = connectorRunner.exchangesConnectors()[exchange]?.getTokens() ?: setOf()

        return tokensByExchange.toFlux().asServerResponse()
    }

    fun getTokensCountByExchange(request: ServerRequest): Mono<ServerResponse> {
        val exchange = try {
            request.pathVariable("exchangeName").toUpperCase()
        } catch (e: IllegalArgumentException) {
            return badRequest().build()
        }

        val tokensCountByExchange = (connectorRunner.exchangesConnectors()[exchange]?.getTokens() ?: setOf()).size.toLong()

        return tokensCountByExchange.toMono().asServerResponse()
    }

    fun getTokensCount(request: ServerRequest): Mono<ServerResponse> {
        val tokensCount = getTokensEntities().size.toLong().toMono()

        return tokensCount.asServerResponse()
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