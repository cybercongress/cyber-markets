package fund.cyber.markets.rest.handler

import com.fasterxml.jackson.databind.ObjectMapper
import fund.cyber.markets.common.booleanValue
import fund.cyber.markets.common.stringValue
import fund.cyber.markets.dao.service.TickerDaoService
import fund.cyber.markets.rest.configuration.AppContext
import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange
import io.undertow.util.Headers
import java.math.BigDecimal

class PriceHandler(
    private val tickerDaoService: TickerDaoService = AppContext.tickerDaoService,
    private val jsonSerializer: ObjectMapper = AppContext.jsonSerializer
) : HttpHandler {

    override fun handleRequest(httpExchange: HttpServerExchange) {

        val params = httpExchange.queryParameters
        val base = params["fsym"]?.stringValue()
        val quotes = params["tsym"]?.stringValue()?.split(",")
        var exchange = params["e"]?.stringValue()
        var tryConversion = params["tryConversion"]?.booleanValue()

        if (base == null || quotes == null) {
            httpExchange.statusCode = 400
            return
        }
        if (exchange == null) {
            exchange = "ALL"
        }
        if (tryConversion == null) {
            tryConversion = true
        }

        val timestamp = System.currentTimeMillis() / 60 / 1000 * 60 * 1000
        val result = mutableMapOf<String, BigDecimal>()

        for (quote in quotes) {
            val ticker = tickerDaoService.getLastMinuteTicker(base, quote, exchange, timestamp)
            if (ticker != null) {
                result.put(quote, ticker.price)
            }
        }

        val rawResponse = jsonSerializer.writeValueAsString(result)

        httpExchange.responseHeaders.put(Headers.CONTENT_TYPE, "application/json")
        httpExchange.responseSender.send(rawResponse)
    }

}