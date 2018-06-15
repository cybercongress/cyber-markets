package fund.cyber.markets.api.rest.handler

import fund.cyber.markets.cassandra.model.CqlTokenTicker
import fund.cyber.markets.cassandra.repository.TickerRepository
import fund.cyber.markets.common.DAYS_TO_MILLIS
import fund.cyber.markets.common.Intervals
import fund.cyber.markets.common.MILLIS_TO_DAYS
import fund.cyber.markets.common.MINUTES_TO_MILLIS
import fund.cyber.markets.common.convert
import fund.cyber.markets.common.rest.asServerResponse
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

const val LIMIT_DEFAULT = "100"

@Component
class TickerHandler(
    private val tickerRepository: TickerRepository
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

        val epochDay = ts convert MILLIS_TO_DAYS
        val iterations = (ts - (epochDay convert DAYS_TO_MILLIS) + interval * limit) / Intervals.DAY

        val dateTs = Date(ts)
        var limitVar = limit

        var tickers = Flux.empty<CqlTokenTicker>()
        for (iteration in 0..iterations) {

            val iterationLimit = getLimit(iteration, epochDay, ts, interval, limitVar)
            limitVar -=iterationLimit

            tickers = tickers
                .concatWith(
                    tickerRepository.find(symbol, (epochDay + iteration), dateTs, interval, iterationLimit)
                )
        }

        return tickers.asServerResponse()
    }

    private fun getLimit(iteration: Long, epochDay: Long, ts: Long, interval: Long, limit: Long): Long {
        var limitResult: Long

        if (iteration == 0L) {
            limitResult = (((epochDay + 1) convert DAYS_TO_MILLIS) - ts) / interval
            if (limitResult > limit) {
                limitResult = limit
            }
        } else {
            limitResult = Intervals.DAY / interval
            if (limitResult > limit) {
                limitResult = limit
            }
        }

        return limitResult
    }

}