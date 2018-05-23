package fund.cyber.markets.api.rest.handler

import fund.cyber.markets.api.rest.configuration.PAGE_DEFAULT
import fund.cyber.markets.api.rest.configuration.PAGE_SIZE_DEFAULT
import fund.cyber.markets.cassandra.model.CqlTokenTicker
import fund.cyber.markets.cassandra.repository.TickerRepository
import fund.cyber.markets.common.model.Exchanges
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.notFound
import org.springframework.web.reactive.function.server.ServerResponse.ok
import reactor.core.publisher.Mono
import java.util.*

@Component
class TickerHandler(
    private val tickerRepository: TickerRepository
) {

    fun getTickers(serverRequest: ServerRequest): Mono<ServerResponse> {
        val symbol: String
        val ts: Long

        try {
            symbol = serverRequest.queryParam("symbol").get().toUpperCase()
            ts = serverRequest.queryParam("ts").get().toLong()
        } catch (e: NoSuchElementException) {
            return ServerResponse.status(HttpStatus.BAD_REQUEST).build()
        }

        val exchange = serverRequest.queryParam("exchange").orElse(Exchanges.ALL).toUpperCase()
        val page = serverRequest.queryParam("page").orElse(PAGE_DEFAULT.toString()).toLong()
        val pageSize = serverRequest.queryParam("pageSize").orElse(PAGE_SIZE_DEFAULT.toString()).toLong()

        //todo: use correct repository call
        val tickers = tickerRepository.findAll()

        return ok()
            .body(tickers, CqlTokenTicker::class.java)
            .switchIfEmpty(
                notFound().build()
            )
    }

}