package fund.cyber.markets.rest.handler

import fund.cyber.markets.cassandra.repository.TickerRepository
import fund.cyber.markets.cassandra.repository.VolumeRepository
import fund.cyber.markets.helpers.MILLIS_TO_SECONDS
import fund.cyber.markets.helpers.booleanValue
import fund.cyber.markets.helpers.closestSmallerMultiply
import fund.cyber.markets.helpers.convert
import fund.cyber.markets.helpers.intValue
import fund.cyber.markets.helpers.longValue
import fund.cyber.markets.helpers.stringValue
import fund.cyber.markets.model.TokensPair
import fund.cyber.markets.rest.configuration.AppContext
import fund.cyber.markets.rest.model.ConversionType
import fund.cyber.markets.rest.model.HistoEntity
import fund.cyber.markets.rest.model.TickerData
import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange
import java.math.BigDecimal

private const val LIMIT_DEFAULT = 1440

class HistoHandler(
        private val duration: Long,
        private val durationSec: Long = duration convert MILLIS_TO_SECONDS,
        private val tickerRepository: TickerRepository = AppContext.tickerRepository,
        private val volumeRepository: VolumeRepository = AppContext.volumeRepository
) : AbstractHandler(), HttpHandler {

    override fun handleRequest(httpExchange: HttpServerExchange) {

        val params = httpExchange.queryParameters
        val base = params["fsym"]?.stringValue()
        val quote = params["tsym"]?.stringValue()
        val exchange = params["e"]?.stringValue() ?: "ALL"
        var tryConversion = params["tryConversion"]?.booleanValue() ?: false
        var limit = params["limit"]?.intValue() ?: LIMIT_DEFAULT
        val timestamp = params["toTs"]?.longValue() ?: getTimestamp()

        if (base == null || quote == null || base == quote) {
            handleBadRequest("Bad parameters", httpExchange)
            return
        }

        val tickers = tickerRepository.getTickers(TokensPair(base, quote), duration, exchange, timestamp, limit).reversed()

        if (tickers.isEmpty()) {
            handleNoData(httpExchange)
            return
        }

        val data = mutableListOf<TickerData>()
        var prevTickerData = TickerData(tickers.first(), BigDecimal.ZERO, BigDecimal.ZERO)
        tickers.forEach { ticker ->

            val volumeBase = volumeRepository.get(ticker.pair.base, exchange, duration, timestamp)
            val volumeQuote = volumeRepository.get(ticker.pair.quote, exchange, duration, timestamp)

            val tickerData = TickerData(ticker, volumeBase?.value, volumeQuote?.value)

            if (tickerData.time - prevTickerData.time > durationSec) {
                var time = prevTickerData.time
                val close = prevTickerData.close
                while (tickerData.time - time > durationSec) {
                    time += durationSec
                    data.add(TickerData(time, close))
                }
            }
            prevTickerData = tickerData

            data.add(tickerData)
        }

        resolveGaps(data, timestamp, limit)

        if (limit > data.size) {
            limit = data.size
        }

        val histoEntity = HistoEntity(
                "Success",
                data.subList(0, limit),
                tickers.last().timestampTo!!.time convert MILLIS_TO_SECONDS,
                tickers.first().timestampTo!!.time convert MILLIS_TO_SECONDS,
                ConversionType("direct", "")
        )

        send(histoEntity, httpExchange)
    }

    private fun getTimestamp(): Long {
        return closestSmallerMultiply(System.currentTimeMillis(), duration) - duration
    }

    private fun resolveGaps(data: MutableList<TickerData>, timestamp: Long, limit: Int) {
        val timestampToSec = closestSmallerMultiply(timestamp, duration) convert MILLIS_TO_SECONDS

        while (data.first().time > timestampToSec) {
            data.add(0, TickerData(data.first().time - durationSec, data.first().close))
        }

        val currentTimestamp = closestSmallerMultiply(System.currentTimeMillis(), duration) convert MILLIS_TO_SECONDS
        while (data.last().time < currentTimestamp && data.size < limit) {
            data.add(TickerData(data.last().time + durationSec, data.last().close))
        }
    }

}