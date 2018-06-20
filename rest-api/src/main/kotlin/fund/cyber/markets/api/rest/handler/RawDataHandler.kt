package fund.cyber.markets.api.rest.handler

import fund.cyber.markets.cassandra.model.CqlOrderBook
import fund.cyber.markets.cassandra.model.CqlTokensPair
import fund.cyber.markets.cassandra.model.CqlTrade
import fund.cyber.markets.cassandra.repository.OrderBookRepository
import fund.cyber.markets.cassandra.repository.TradeRepository
import fund.cyber.markets.common.MILLIS_TO_HOURS
import fund.cyber.markets.common.convert
import fund.cyber.markets.common.model.OrderBook
import fund.cyber.markets.common.model.TokensPair
import fund.cyber.markets.common.rest.service.ConnectorService
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
    private val connectorService: ConnectorService,
    private val orderBookRepository: OrderBookRepository,
    private val tradeRepository: TradeRepository
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

        val tokensPair = TokensPair(pair)

        if (ts != null) {
            val orderBook = orderBookRepository
                .findLastByTimestamp(exchange, CqlTokensPair(tokensPair), ts convert MILLIS_TO_HOURS, Date(ts))

            return ok()
                .body(orderBook, CqlOrderBook::class.java)
                .switchIfEmpty(
                    notFound().build()
                )

        } else {
            val orderBook = connectorService.getOrderBook(exchange, tokensPair)

            return ok()
                .body(orderBook, OrderBook::class.java)
                .switchIfEmpty(
                    notFound().build()
                )
        }

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
            .body(tradeRepository.find(exchange, CqlTokensPair(pair), epochMin), CqlTrade::class.java)
            .switchIfEmpty(
                notFound().build()
            )
    }

}