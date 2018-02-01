package fund.cyber.markets.rest.handler

import fund.cyber.markets.cassandra.repository.TickerRepository
import fund.cyber.markets.cassandra.repository.VolumeRepository
import fund.cyber.markets.common.Durations
import fund.cyber.markets.common.booleanValue
import fund.cyber.markets.common.stringValue
import fund.cyber.markets.dto.TokensPair
import fund.cyber.markets.rest.configuration.AppContext
import fund.cyber.markets.rest.model.PriceMultiFullData
import fund.cyber.markets.rest.model.PriceMultiFullModel
import fund.cyber.markets.util.closestSmallerMultiply
import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange

class PriceMultiFullHandler(
    private val tickerRepository: TickerRepository = AppContext.tickerRepository,
    private val volumeRepository: VolumeRepository = AppContext.volumeRepository
    ) : AbstractHandler(), HttpHandler {

    override fun handleRequest(httpExchange: HttpServerExchange) {

        val params = httpExchange.queryParameters
        val bases = params["fsyms"]?.stringValue()?.split(",")
        val quotes = params["tsyms"]?.stringValue()?.split(",")
        val exchange = params["e"]?.stringValue() ?: "ALL"
        val tryConversion = params["tryConversion"]?.booleanValue() ?: false

        if (bases == null || quotes == null) {
            handleBadRequest("Bad parameters", httpExchange)
            return
        }

        val timestamp = closestSmallerMultiply(System.currentTimeMillis(), Durations.MINUTE)
        val raw = mutableMapOf<String, MutableMap<String, PriceMultiFullData>>()

        if (!tryConversion) {
            for (base in bases) {
                val quoteFullData = mutableMapOf<String, PriceMultiFullData>()
                for (quote in quotes) {
                    if (base != quote) {
                        val ticker = tickerRepository.getTicker(TokensPair(base, quote), Durations.MINUTE, exchange, timestamp)
                        val ticker24h = tickerRepository.getTicker24h(TokensPair(base, quote), exchange)

                        val volumeBase = volumeRepository.getVolume24h(base, exchange)
                        val volumeQuote = volumeRepository.getVolume24h(quote, exchange)

                        if (ticker != null && ticker24h != null) {
                            val priceData = PriceMultiFullData(
                                    exchange,
                                    base,
                                    quote,
                                    ticker.close,
                                    ticker.timestampTo!!.time / 1000,
                                    volumeBase?.value,
                                    volumeQuote?.value,
                                    ticker24h.open,
                                    ticker24h.maxPrice,
                                    ticker24h.minPrice
                            )
                            quoteFullData.put(quote, priceData)
                        }
                    }
                }
                raw.put(base, quoteFullData)
            }
        } else {
            // TODO: implement cross conversion for historical data
        }

        if (raw.isEmpty()) {
            handleNoData(httpExchange)
            return
        }

        send(PriceMultiFullModel(raw), httpExchange)
    }

}