package fund.cyber.markets.rest.handler

import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange

class PingPongHandler : HttpHandler {

    override fun handleRequest(exchange: HttpServerExchange) {
        exchange.statusCode = 200
    }

}