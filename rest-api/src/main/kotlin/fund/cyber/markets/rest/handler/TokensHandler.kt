package fund.cyber.markets.rest.handler

import fund.cyber.markets.cassandra.repository.SupplyRepository
import fund.cyber.markets.cassandra.repository.TickerRepository
import fund.cyber.markets.cassandra.repository.VolumeRepository
import fund.cyber.markets.common.stringValue
import fund.cyber.markets.dto.TokensPair
import fund.cyber.markets.rest.configuration.AppContext
import fund.cyber.markets.rest.model.TokenModel
import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange
import java.math.BigDecimal

class TokensHandler(
    private val tickerRepository: TickerRepository = AppContext.tickerRepository,
    private val volumeRepository: VolumeRepository = AppContext.volumeRepository,
    private val supplyRepository: SupplyRepository = AppContext.supplyRepository
) : AbstractHandler(), HttpHandler {

    override fun handleRequest(httpExchange: HttpServerExchange) {

        val params = httpExchange.queryParameters
        val pairs = params["fsyms"]?.stringValue()?.split(",")

        if (pairs == null) {
            handleBadRequest("Bad parameters", httpExchange)
            return
        }

        val result = mutableListOf<TokenModel>()

        pairs.forEach { pair ->
            val base = pair.substringBefore("_")
            val quote = pair.substringAfter("_")

            val ticker = tickerRepository.getTicker24h(TokensPair(base, quote), "ALL")
            val volume = volumeRepository.getVolume24h(base, "ALL")
            val supply = supplyRepository.get(base)

            result.add(TokenModel(
                    base,
                    quote,
                    supply?.value,
                    ticker?.close,
                    volume?.value,
                    ticker?.close?.minus(ticker.open)?.div(ticker.open)?.multiply(BigDecimal(100L))
            ))
        }

        if (result.isEmpty()) {
            handleNoData(httpExchange)
            return
        }

        send(result, httpExchange)
    }

}