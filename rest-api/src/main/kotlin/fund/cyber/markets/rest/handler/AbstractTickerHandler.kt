package fund.cyber.markets.rest.handler

import com.fasterxml.jackson.databind.ObjectMapper
import fund.cyber.markets.common.stringValue
import io.undertow.server.HttpServerExchange
import io.undertow.util.Headers

open class AbstractTickerHandler(
        val jsonSerializer: ObjectMapper = ObjectMapper()
) {

    fun getParams(httpExchange: HttpServerExchange) : MutableMap<String, String?> {
        val queryParams = httpExchange.queryParameters

        val params = mutableMapOf<String, String?>()
        params.put("base", queryParams["fsym"]?.stringValue())
        params.put("quote", queryParams["tsym"]?.stringValue())
        params.put("exchange", queryParams["e"]?.stringValue())
        params.put("tryConversion", queryParams["tryConversion"]?.stringValue())
        params.put("limit", queryParams["limit"]?.stringValue())
        params.put("timestamp", queryParams["toTs"]?.stringValue())

        return params
    }

    fun send(result: Any, httpExchange: HttpServerExchange) {
        httpExchange.responseHeaders.put(Headers.CONTENT_TYPE, "application/json")
        httpExchange.responseSender.send(jsonSerializer.writeValueAsString(result))
    }

}