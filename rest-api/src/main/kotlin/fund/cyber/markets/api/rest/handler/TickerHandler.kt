package fund.cyber.markets.api.rest.handler

import fund.cyber.markets.api.rest.service.TickerService
import fund.cyber.markets.common.MINUTES_TO_MILLIS
import fund.cyber.markets.common.convert
import fund.cyber.markets.common.rest.asServerResponse
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import java.util.*

const val LIMIT_DEFAULT = "100"

@Component
class TickerHandler(
    private val tickerService: TickerService
) {

    fun getTickers(serverRequest: ServerRequest): Mono<ServerResponse> {
        val symbol: String
        val ts: Long
        val interval: Long

        val limit = serverRequest.queryParam("limit").orElse(LIMIT_DEFAULT).toLong()
        try {
            symbol = serverRequest.queryParam("symbol").get().toUpperCase()
            ts = serverRequest.queryParam("ts").get().toLong()
            interval = serverRequest.queryParam("interval").get().toLong() convert MINUTES_TO_MILLIS
        } catch (e: NoSuchElementException) {
            return ServerResponse.status(HttpStatus.BAD_REQUEST).build()
        }

        return tickerService.getTickers(symbol, ts, interval, limit).asServerResponse()
    }

}