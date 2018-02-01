package fund.cyber.markets.rest.handler

import fund.cyber.markets.rest.util.CryptoProxy
import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange

class TokenListHandler: AbstractHandler(), HttpHandler {

    override fun handleRequest(httpExchange: HttpServerExchange) {

        val result = CryptoProxy.getTokens()

        if (result.isEmpty()) {
            handleNoData(httpExchange)
            return
        }

        send(result, httpExchange)
    }

}