package fund.cyber.markets.rest.handler

import fund.cyber.markets.cassandra.repository.TickerRepository
import fund.cyber.markets.common.Durations
import fund.cyber.markets.common.booleanValue
import fund.cyber.markets.common.stringValue
import fund.cyber.markets.dto.TokensPair
import fund.cyber.markets.rest.common.CrossConversion
import fund.cyber.markets.rest.configuration.AppContext
import fund.cyber.markets.util.closestSmallerMultiply
import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange
import java.math.BigDecimal

class PriceHandler(
    private val tickerRepository: TickerRepository = AppContext.tickerRepository
) : AbstractHandler(), HttpHandler {

    override fun handleRequest(httpExchange: HttpServerExchange) {

        val params = httpExchange.queryParameters
        val base = params["fsym"]?.stringValue()
        val quotes = params["tsyms"]?.stringValue()?.split(",")
        val exchange = params["e"]?.stringValue() ?: "ALL"
        val tryConversion = params["tryConversion"]?.booleanValue() ?: true

        if (base == null || quotes == null) {
            handleBadRequest("Bad parameters", httpExchange)
            return
        }

        val timestamp = closestSmallerMultiply(System.currentTimeMillis(), Durations.MINUTE)
        val result = mutableMapOf<String, BigDecimal>()

        for (quote in quotes) {
            if (base != quote) {
                val ticker = tickerRepository.getTicker(TokensPair(base, quote), Durations.MINUTE, exchange, timestamp)
                if (ticker != null) {
                    result.put(quote, ticker.close)
                } else if (tryConversion) {
                    val conversion = CrossConversion(tickerRepository, base, quote, exchange, Durations.MINUTE, timestamp).calculate()
                    if (conversion.success) {
                        result.put(quote, conversion.value!!)
                    }
                }
            }
        }

        if (result.isEmpty()) {
            handleNoData(httpExchange)
            return
        }

        send(result, httpExchange)
    }

}