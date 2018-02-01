package fund.cyber.markets.rest.handler

import fund.cyber.markets.cassandra.repository.TickerRepository
import fund.cyber.markets.cassandra.repository.VolumeRepository
import fund.cyber.markets.common.stringValue
import fund.cyber.markets.dto.TokensPair
import fund.cyber.markets.rest.configuration.AppContext
import fund.cyber.markets.rest.model.TokenModel
import fund.cyber.markets.rest.util.CryptoProxy
import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange

class TokensHandler(
    private val tickerRepository: TickerRepository = AppContext.tickerRepository,
    private val volumeRepository: VolumeRepository = AppContext.volumeRepository
) : AbstractHandler(), HttpHandler {

    override fun handleRequest(httpExchange: HttpServerExchange) {

        val params = httpExchange.queryParameters
        val pairs = params["fsyms"]?.stringValue()?.split(",")

        if (pairs == null) {
            handleBadRequest("Bad parameters", httpExchange)
            return
        }

        val result = mutableListOf<TokenModel>()
        val supplies = CryptoProxy.getSupplies()

        pairs.forEach { pair ->
            val base = pair.substringBefore("_")
            val quote = pair.substringAfter("_")

            val ticker = tickerRepository.getTicker24h(TokensPair(base, quote), "ALL")
            val volume = volumeRepository.getVolume24h(base, "ALL")

/*            result.add(TokenModel(
                    base,
                    quote,
                    supplies[base],
                    ticker?.close,
                    volume?.value,
                    ticker?.close?.minus(ticker.open)?.div(ticker.open)?.multiply(BigDecimal(100L))
            ))*/
        }

        if (result.isEmpty()) {
            handleNoData(httpExchange)
            return
        }

        send(result, httpExchange)
    }

}