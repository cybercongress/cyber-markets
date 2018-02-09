package fund.cyber.markets.rest.handler

import fund.cyber.markets.cassandra.repository.SupplyRepository
import fund.cyber.markets.cassandra.repository.TickerRepository
import fund.cyber.markets.cassandra.repository.VolumeRepository
import fund.cyber.markets.common.Durations
import fund.cyber.markets.dto.TokensPair
import fund.cyber.markets.helpers.closestSmallerMultiply
import fund.cyber.markets.helpers.stringValue
import fund.cyber.markets.model.TokenSupply
import fund.cyber.markets.model.TokenVolume
import fund.cyber.markets.rest.common.CrossConversion
import fund.cyber.markets.rest.configuration.AppContext
import fund.cyber.markets.rest.model.TokenDetailsModel
import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange
import java.math.BigDecimal
import java.util.*

class TokenDetailsHandler(
    private val tickerRepository: TickerRepository = AppContext.tickerRepository,
    private val volumeRepository: VolumeRepository = AppContext.volumeRepository,
    private val supplyRepository: SupplyRepository = AppContext.supplyRepository
    ) : AbstractHandler(), HttpHandler {

    override fun handleRequest(httpExchange: HttpServerExchange) {

        val params = httpExchange.queryParameters
        val fromSymbol = params["fsym"]?.stringValue()
        val timestamp = closestSmallerMultiply(System.currentTimeMillis(), Durations.MINUTE)

        if (fromSymbol == null || fromSymbol.trim().isEmpty()) {
            handleBadRequest("Bad parameters", httpExchange)
            return
        }

        val prices = mutableMapOf<String, BigDecimal>()
        val toSymbols = mutableListOf("USD", "USDT", "BTC", "ETH")
        toSymbols.forEach { toSymbol ->
            val price = getPrice(fromSymbol, toSymbol, timestamp)
            if (price != null) {
                prices[toSymbol] = price
            }
        }
        if (prices["USD"] == null && prices["USDT"] != null) {
            prices["USD"] = prices["USDT"]!!
        }

        val supply = supplyRepository.get(fromSymbol) ?: TokenSupply(fromSymbol, BigDecimal.ZERO, BigDecimal.ZERO)
        val volume = volumeRepository.getVolume24h(fromSymbol, "ALL") ?: TokenVolume(fromSymbol, Durations.DAY, "ALL", BigDecimal.ZERO, Date(), Date())

        val capUsd = supply.value.multiply(prices["USD"])
        val capBtc = supply.value.multiply(prices["BTC"] ?: BigDecimal.ONE)

        val tokenDetails = TokenDetailsModel(
                prices["USD"]!!,
                prices["BTC"] ?: BigDecimal.ONE,
                prices["ETH"] ?: BigDecimal.ONE,
                capUsd,
                capBtc,
                volume.value.multiply(prices["USD"]),
                volume.value,
                supply.totalValue,
                supply.value
        )

        send(tokenDetails, httpExchange)
    }

    private fun getPrice(fromSymbol: String, toSymbol: String, timestamp: Long): BigDecimal? {

        var price = tickerRepository.getTicker(
                TokensPair(fromSymbol, toSymbol),
                Durations.MINUTE,
                "ALL",
                timestamp)?.close

        if (price == null) {
            val crossConversion = CrossConversion(tickerRepository, fromSymbol, toSymbol, "ALL", Durations.MINUTE, timestamp).calculate()
            if (crossConversion.success) {
                price = crossConversion.value
            }
        }

        return price
    }

}