package fund.cyber.markets.api.rest.service

import fund.cyber.markets.cassandra.model.CqlTokenTicker
import fund.cyber.markets.cassandra.repository.TickerRepository
import fund.cyber.markets.common.DAYS_TO_MILLIS
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
        var tickers = Flux.empty<CqlTokenTicker>()

        var tsVar = ts
        var limitVar = limit

        while (limitVar > 0) {

            val epochDay = tsVar convert MILLIS_TO_DAYS
            var iterationLimit = (((epochDay + 1) convert DAYS_TO_MILLIS) - tsVar) / interval

            if (iterationLimit > limitVar) {
                iterationLimit = limitVar
            }

            if (iterationLimit > 0) {
                tickers = tickers
                    .concatWith(
                        tickerRepository.find(symbol, epochDay, Date(ts), interval, iterationLimit)
                    )
            } else {
                tsVar = (epochDay + 1) convert DAYS_TO_MILLIS
            }

            tsVar += interval * iterationLimit
            limitVar -= iterationLimit
        }

        return tickers
    }
}