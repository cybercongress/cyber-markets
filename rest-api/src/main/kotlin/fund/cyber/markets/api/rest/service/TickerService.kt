package fund.cyber.markets.api.rest.service

import fund.cyber.markets.cassandra.model.CqlTokenTicker
import fund.cyber.markets.cassandra.repository.TickerRepository
import fund.cyber.markets.common.DAYS_TO_MILLIS
import fund.cyber.markets.common.Intervals
import fund.cyber.markets.common.MILLIS_TO_DAYS
import fund.cyber.markets.common.convert
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import java.util.*

@Service
class TickerService(
    private val tickerRepository: TickerRepository
) {

    fun getTickers(symbol: String, ts: Long, interval: Long, limit: Long): Flux<CqlTokenTicker> {
        val epochDay = ts convert MILLIS_TO_DAYS
        val iterations = (ts - (epochDay convert DAYS_TO_MILLIS) + interval * limit) / Intervals.DAY

        val dateTs = Date(ts)
        var limitVar = limit

        var tickers = Flux.empty<CqlTokenTicker>()
        for (iteration in 0..iterations) {

            val iterationLimit = getLimit(iteration, epochDay, ts, interval, limitVar)
            limitVar -= iterationLimit

            if (iterationLimit > 0) {
                tickers = tickers
                    .concatWith(
                        tickerRepository.find(symbol, (epochDay + iteration), dateTs, interval, iterationLimit)
                    )
            }
        }

        return tickers
    }

    private fun getLimit(iteration: Long, epochDay: Long, ts: Long, interval: Long, limit: Long): Long {

        var limitResult = if (iteration == 0L) {
            (((epochDay + 1) convert DAYS_TO_MILLIS) - ts) / interval
        } else {
            Intervals.DAY / interval
        }

        if (limitResult > limit) {
            limitResult = limit
        }

        return limitResult
    }
}