package fund.cyber.markets.rest.handler

import io.undertow.server.HttpServerExchange
import io.undertow.util.HttpString

fun HttpServerExchange.setAccessHeaders() {
    this.responseHeaders.put(HttpString("Access-Control-Allow-Origin"), "*")
    this.responseHeaders.put(HttpString("Access-Control-Allow-Headers"), "Origin, X-Requested-With, Content-Type, Accept")
}