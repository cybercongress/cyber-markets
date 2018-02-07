package fund.cyber.markets.rest.handler

import fund.cyber.markets.cassandra.repository.TickerRepository
import fund.cyber.markets.cassandra.repository.VolumeRepository
import fund.cyber.markets.common.booleanValue
import fund.cyber.markets.common.closestSmallerMultiply
import fund.cyber.markets.common.intValue
import fund.cyber.markets.common.longValue
import fund.cyber.markets.common.stringValue
import fund.cyber.markets.dto.TokensPair
import fund.cyber.markets.rest.configuration.AppContext
import fund.cyber.markets.rest.model.ConversionType
import fund.cyber.markets.rest.model.HistoEntity
import fund.cyber.markets.rest.model.TickerData
import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange

class HistoHandler(
        private val duration: Long,
        private val tickerRepository: TickerRepository = AppContext.tickerRepository,
        private val volumeRepository: VolumeRepository = AppContext.volumeRepository
) : AbstractHandler(), HttpHandler {

    override fun handleRequest(httpExchange: HttpServerExchange) {

        val params = httpExchange.queryParameters
        val base = params["fsym"]?.stringValue()
        val quote = params["tsym"]?.stringValue()
        val exchange = params["e"]?.stringValue() ?: "ALL"
        var tryConversion = params["tryConversion"]?.booleanValue() ?: false
        val limit = params["limit"]?.intValue() ?: 1440
        val timestamp = params["toTs"]?.longValue() ?: getTimestamp()

        if (base == null || quote == null || base == quote) {
            handleBadRequest("Bad parameters", httpExchange)
            return
        }

        val tickers = tickerRepository.getTickers(TokensPair(base, quote), duration, exchange, timestamp, limit)

        if (tickers.isEmpty()) {
            handleNoData(httpExchange)
            return
        }

        val data = mutableListOf<TickerData>()
        tickers.forEach { ticker ->

            val volumeBase = volumeRepository.get(ticker.pair.base, exchange, duration, timestamp)
            val volumeQuote = volumeRepository.get(ticker.pair.quote, exchange, duration, timestamp)

            val tickerData = TickerData(
                    ticker.timestampTo?.time!! / 1000,
                    ticker.open,
                    ticker.close,
                    ticker.maxPrice,
                    ticker.minPrice,
                    volumeBase?.value,
                    volumeQuote?.value
            )
            data.add(tickerData)
        }

        val histoEntity = HistoEntity(
                "Success",
                data,
                tickers.last().timestampTo!!.time / 1000,
                timestamp / 1000,
                ConversionType("direct", "")
        )

        send(histoEntity, httpExchange)
    }

    private fun getTimestamp(): Long {
        return closestSmallerMultiply(System.currentTimeMillis(),duration) - duration
    }

}