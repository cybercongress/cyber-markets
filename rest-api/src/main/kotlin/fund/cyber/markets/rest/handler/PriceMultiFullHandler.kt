package fund.cyber.markets.rest.handler

import fund.cyber.markets.common.booleanValue
import fund.cyber.markets.common.stringValue
import fund.cyber.markets.dao.service.TickerDaoService
import fund.cyber.markets.rest.configuration.AppContext
import fund.cyber.markets.rest.model.PriceMultiFullData
import fund.cyber.markets.rest.model.PriceMultiFullModel
import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange

class PriceMultiFullHandler(
    private val tickerDaoService: TickerDaoService = AppContext.tickerDaoService
) : AbstractHandler(), HttpHandler {

    override fun handleRequest(httpExchange: HttpServerExchange) {

        val params = httpExchange.queryParameters
        val bases = params["fsyms"]?.stringValue()?.split(",")
        val quotes = params["tsyms"]?.stringValue()?.split(",")
        var exchange = params["e"]?.stringValue()
        var tryConversion = params["tryConversion"]?.booleanValue()

        if (bases == null || quotes == null) {
            handleBadRequest("Bad parameters", httpExchange)
        }
        if (exchange == null) {
            exchange = "ALL"
        }
        if (tryConversion == null) {
            tryConversion = false
        }

        val timestamp = System.currentTimeMillis() / 60 / 1000 * 60 * 1000
        val raw = mutableMapOf<String, MutableMap<String, PriceMultiFullData>>()


        if (!tryConversion) {
            for (base in bases!!) {
                val quoteFullData = mutableMapOf<String, PriceMultiFullData>()
                for (quote in quotes!!) {
                    if (base != quote) {
                        val ticker = tickerDaoService.getMinuteTicker(base, quote, exchange, timestamp)
                        val ticker24h = tickerDaoService.getLastDayTicker(base, quote, exchange)
                        if (ticker != null && ticker24h != null) {
                            val priceData = PriceMultiFullData(
                                    exchange,
                                    base,
                                    quote,
                                    ticker.price,
                                    ticker.timestampTo!!.time,
                                    ticker24h.baseAmount,
                                    ticker24h.quoteAmount,
                                    ticker24h.maxPrice!!,
                                    ticker24h.minPrice!!
                            )
                            quoteFullData.put(quote, priceData)
                        }
                    }
                }
                raw.put(base, quoteFullData)
            }
        }

        if (raw.isEmpty()) {
            handleNoData(httpExchange)
            return
        }

        send(PriceMultiFullModel(raw), httpExchange)
    }

}