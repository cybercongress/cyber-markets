package fund.cyber.markets.rest.handler

import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange
import io.undertow.util.HttpString


class SetCorsHeadersHandler(
        private val next: HttpHandler,
        private val allowedOrigins: String
) : HttpHandler {

    override fun handleRequest(exchange: HttpServerExchange) {
        exchange.responseHeaders.put(HttpString("Access-Control-Allow-Origin"), allowedOrigins)
        next.handleRequest(exchange)
    }
}