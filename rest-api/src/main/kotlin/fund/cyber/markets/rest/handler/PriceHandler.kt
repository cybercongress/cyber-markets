package fund.cyber.markets.rest.handler

import fund.cyber.markets.common.booleanValue
import fund.cyber.markets.common.stringValue
import fund.cyber.markets.dao.service.TickerDaoService
import fund.cyber.markets.rest.configuration.AppContext
import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange
import java.math.BigDecimal

class PriceHandler(
    private val tickerDaoService: TickerDaoService = AppContext.tickerDaoService
) : AbstractHandler(), HttpHandler {

    override fun handleRequest(httpExchange: HttpServerExchange) {

        val params = httpExchange.queryParameters
        val base = params["fsym"]?.stringValue()
        val quotes = params["tsyms"]?.stringValue()?.split(",")
        var exchange = params["e"]?.stringValue()
        var tryConversion = params["tryConversion"]?.booleanValue()

        if (base == null || quotes == null) {
            handleBadRequest("Bad parameters", httpExchange)
        }
        if (exchange == null) {
            exchange = "ALL"
        }
        if (tryConversion == null) {
            tryConversion = false
        }

        val timestamp = System.currentTimeMillis() / 60 / 1000 * 60 * 1000
        val result = mutableMapOf<String, BigDecimal>()

        if (!tryConversion) {
            for (quote in quotes!!) {
                if (base!! != quote) {
                    val ticker = tickerDaoService.getMinuteTicker(base, quote, exchange, timestamp)
                    if (ticker != null) {
                        result.put(quote, ticker.price)
                    }
                }
            }
        }

        if (result.isEmpty()) {
            handleNoData(httpExchange)
        }

        send(result, httpExchange)
    }

}