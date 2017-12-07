package fund.cyber.markets.rest

import fund.cyber.markets.rest.configuration.RestApiConfiguration
import fund.cyber.markets.rest.handler.HistoMinuteHandler
import fund.cyber.markets.rest.handler.PingPongHandler
import fund.cyber.markets.rest.handler.SetCorsHeadersHandler
import fund.cyber.markets.rest.handler.TokenStatsHandler
import io.undertow.Handlers
import io.undertow.Undertow

fun main(args: Array<String>) {

    val httpHandler = Handlers.routing()
            .get("/tokenstats", TokenStatsHandler())
            .get("/ping", PingPongHandler())
            .get("/histominute", HistoMinuteHandler())

    val setCorsHeaderHandler = SetCorsHeadersHandler(httpHandler, RestApiConfiguration.allowedCORS)

    Undertow.builder()
            .addHttpListener(8085, "0.0.0.0")
            .setHandler(setCorsHeaderHandler)
            .build().start()
}