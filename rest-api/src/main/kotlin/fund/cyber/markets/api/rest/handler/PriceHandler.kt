package fund.cyber.markets.api.rest.handler

import fund.cyber.markets.api.rest.service.PriceService
import fund.cyber.markets.common.model.Exchanges
import fund.cyber.markets.common.rest.asServerResponseAllowEmpty
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import java.util.*


@Component
class PriceHandler(
    private val priceService: PriceService
) {

    fun getPrices(serverRequest: ServerRequest): Mono<ServerResponse> {

        val base: String
        val quotes: List<String>
        val exchange: String

        try {
            base = serverRequest.queryParam("base").get().toUpperCase()
            quotes = serverRequest
                .queryParam("quote")
                .get()
                .toUpperCase()
                .split(",")
            exchange = serverRequest.queryParam("exchange").orElse(Exchanges.ALL).toUpperCase()
        } catch (e: NoSuchElementException) {
            return ServerResponse.status(HttpStatus.BAD_REQUEST).build()
        }

        return priceService.getPrices(base, quotes, exchange).asServerResponseAllowEmpty()
    }

}