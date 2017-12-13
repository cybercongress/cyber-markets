package fund.cyber.markets.rest.handler

import fund.cyber.markets.dao.service.TickerDaoService
import fund.cyber.markets.rest.configuration.AppContext
import fund.cyber.markets.rest.model.StatsModel
import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange
import java.math.BigDecimal
import java.util.*


class StatsHandler(
        private val tickerDaoService: TickerDaoService = AppContext.tickerDaoService
) : AbstractHandler(), HttpHandler {

    private val random = Random()

    override fun handleRequest(exchange: HttpServerExchange) {
        send(StatsModel(rand(2000000, 10000000), rand(-5, 5), rand(10000, 30000), rand(-7, 7)), exchange)
    }

    private fun rand(from: Int, to: Int) : BigDecimal {
        return BigDecimal(random.nextInt(to - from) + from)
    }
}

