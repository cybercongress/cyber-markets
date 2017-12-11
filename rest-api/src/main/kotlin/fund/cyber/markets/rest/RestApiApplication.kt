package fund.cyber.markets.rest

import fund.cyber.markets.rest.configuration.RestApiConfiguration
import fund.cyber.markets.rest.handler.HistoHandler
import fund.cyber.markets.rest.handler.PingPongHandler
import fund.cyber.markets.rest.handler.PriceHandler
import fund.cyber.markets.rest.handler.PriceMultiHandler
import fund.cyber.markets.rest.handler.SetCorsHeadersHandler
import fund.cyber.markets.rest.handler.TokenStatsHandler
import io.undertow.Handlers
import io.undertow.Undertow
import java.util.concurrent.TimeUnit


fun main(args: Array<String>) {

    val httpHandler = Handlers.routing()
            .get("/tokenstats", TokenStatsHandler())
            .get("/ping", PingPongHandler())
            .get("/histominute", HistoHandler(TimeUnit.MINUTES.toMillis(1)))
            .get("/histohour", HistoHandler(TimeUnit.HOURS.toMillis(1)))
            .get("/histoday", HistoHandler(TimeUnit.DAYS.toMillis(1)))
            .get("/price", PriceHandler())
            .get("/pricemulti", PriceMultiHandler())


    val setCorsHeaderHandler = SetCorsHeadersHandler(httpHandler, RestApiConfiguration.allowedCORS)

    Undertow.builder()
            .addHttpListener(8085, "0.0.0.0")
            .setHandler(setCorsHeaderHandler)
            .build().start()
}