package fund.cyber.markets.api.rest.handler

import fund.cyber.markets.api.rest.service.RawDataService
import fund.cyber.markets.cassandra.model.CqlTrade
import fund.cyber.markets.common.model.OrderBook
import fund.cyber.markets.common.model.TokensPair
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.badRequest
import org.springframework.web.reactive.function.server.ServerResponse.notFound
import org.springframework.web.reactive.function.server.ServerResponse.ok
import reactor.core.publisher.Mono
import java.util.*

@Component
class RawDataHandler(
    private val rawDataService: RawDataService
) {

    fun getOrderBook(serverRequest: ServerRequest): Mono<ServerResponse> {
        val exchange: String
        val pair: String
        var ts: Long? = null

        try {
            exchange = serverRequest.queryParam("exchange").get().toUpperCase()
            pair = serverRequest.queryParam("pair").get().toUpperCase()
        } catch (e: NoSuchElementException) {
            return ServerResponse.status(HttpStatus.BAD_REQUEST).build()
        }

        try {
            ts = serverRequest.queryParam("ts").get().toLong()
        } catch (e: NoSuchElementException) {
            // nothing
        }

        return ok()
            .body(rawDataService.getOrderBook(exchange, TokensPair(pair), ts), OrderBook::class.java)
            .switchIfEmpty(
                notFound().build()
            )

    }

    fun getTrades(serverRequest: ServerRequest): Mono<ServerResponse> {
        val exchange: String
        val pair: String
        val epochMin: Long

        try {
            exchange = serverRequest.queryParam("exchange").get().toUpperCase()
            pair = serverRequest.queryParam("pair").get().toUpperCase()
            epochMin = serverRequest.queryParam("epochMin").get().toLong()
        } catch (e: NoSuchElementException) {
            return badRequest().build()
        }

        return ok()
            .body(rawDataService.getTrades(exchange, TokensPair(pair), epochMin), CqlTrade::class.java)
            .switchIfEmpty(
                notFound().build()
            )
    }

}