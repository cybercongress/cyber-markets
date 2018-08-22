package fund.cyber.markets.api.rest.handler

import fund.cyber.markets.common.rest.asServerResponse
import fund.cyber.markets.common.rest.service.ConnectorService
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono

@Component
class TokenHandler(
    private val connectorService: ConnectorService
) {

    fun getTokenInfo(request: ServerRequest): Mono<ServerResponse> {

        val tokenSymbol = request
            .pathVariable("tokenSymbol")
            .toUpperCase()

        return connectorService
            .getTokens()
            .filter { token -> token.symbol == tokenSymbol }
            .toMono()
            .asServerResponse()
    }

}
