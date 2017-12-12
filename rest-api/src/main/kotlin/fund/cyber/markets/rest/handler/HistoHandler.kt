package fund.cyber.markets.rest.handler

import fund.cyber.markets.common.booleanValue
import fund.cyber.markets.common.intValue
import fund.cyber.markets.common.longValue
import fund.cyber.markets.common.stringValue
import fund.cyber.markets.dao.service.TickerDaoService
import fund.cyber.markets.rest.configuration.AppContext
import fund.cyber.markets.rest.model.ConversionType
import fund.cyber.markets.rest.model.HistoEntity
import fund.cyber.markets.rest.model.TickerData
import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange

class HistoHandler(
    private val duration: Long,
    private val tickerDaoService: TickerDaoService = AppContext.tickerDaoService
) : AbstractHandler(), HttpHandler {

    override fun handleRequest(httpExchange: HttpServerExchange) {

        val params = httpExchange.queryParameters
        val base = params["fsym"]?.stringValue()
        val quote = params["tsym"]?.stringValue()
        var exchange = params["e"]?.stringValue()
        var tryConversion = params["tryConversion"]?.booleanValue()
        var limit = params["limit"]?.intValue()
        var timestamp = params["toTs"]?.longValue()

        if (base == null || quote == null) {
            handleBadRequest("Bad parameters", httpExchange)
        }
        if (exchange == null) {
            exchange = "ALL"
        }
        if (tryConversion == null) {
            tryConversion = true
        }
        if (limit == null) {
            limit = 1440
        }
        if (timestamp == null) {
            timestamp = System.currentTimeMillis() / duration * duration - duration
        }

        val tickers = tickerDaoService.getTickers(base!!, quote!!, duration, exchange, timestamp, limit)

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

        send(histoEntity, httpExchange)
    }

}