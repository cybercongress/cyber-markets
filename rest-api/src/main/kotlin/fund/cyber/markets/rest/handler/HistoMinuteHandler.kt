package fund.cyber.markets.rest.handler

import com.fasterxml.jackson.databind.ObjectMapper
import fund.cyber.markets.common.booleanValue
import fund.cyber.markets.common.intValue
import fund.cyber.markets.common.longValue
import fund.cyber.markets.common.stringValue
import fund.cyber.markets.dao.service.TickerDaoService
import fund.cyber.markets.dto.TokensPair
import fund.cyber.markets.rest.configuration.AppContext
import fund.cyber.markets.rest.model.ConversionType
import fund.cyber.markets.rest.model.HistoEntity
import fund.cyber.markets.rest.model.TickerData
import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange
import io.undertow.util.Headers

class HistoMinuteHandler(
        private val tickerDaoService: TickerDaoService = AppContext.tickerDaoService,
        private val jsonSerializer: ObjectMapper = AppContext.jsonSerializer
) : HttpHandler {

    val MINUTE_DURATION = 60 * 1000

    override fun handleRequest(httpExchange: HttpServerExchange) {

        val params = httpExchange.queryParameters
        val base = params["fsym"]?.stringValue()
        val quote = params["tsym"]?.stringValue()
        var exchange = params["e"]?.stringValue()
        var tryConversion = params["tryConversion"]?.booleanValue()
        var limit = params["limit"]?.intValue()
        var timestamp = params["toTs"]?.longValue()

        if (base == null || quote == null || exchange == null) {
            httpExchange.statusCode = 400
            return
        } else {
            exchange = exchange
        }
        if (tryConversion == null) {
            tryConversion = true
        }
        if (limit == null) {
            limit = 1440
        }
        if (timestamp == null) {
            timestamp = System.currentTimeMillis() / MINUTE_DURATION * MINUTE_DURATION - MINUTE_DURATION
        }

        val tickers = tickerDaoService.getTickers(TokensPair(base, quote), MINUTE_DURATION, exchange, timestamp, limit)

        val data = mutableListOf<TickerData>()
        tickers.forEach { ticker ->
            val tickerData = TickerData(
                    ticker.timestampTo?.time!!,
                    ticker.maxPrice!!,
                    ticker.minPrice!!,
                    ticker.baseAmount,
                    ticker.quoteAmount
            )
            data.add(tickerData)
        }

        val histoEntity = HistoEntity(
                "Success",
                data,
                tickers.last().timestampTo!!.time,
                timestamp,
                ConversionType("direct", "")
        )

        val rawResponse = jsonSerializer.writeValueAsString(histoEntity)

        httpExchange.responseHeaders.put(Headers.CONTENT_TYPE, "application/json")
        httpExchange.responseSender.send(rawResponse)
    }

}