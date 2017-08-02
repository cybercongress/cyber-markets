package fund.cyber.markets

import com.fasterxml.jackson.databind.ObjectMapper
import fund.cyber.markets.api.common.RootWebSocketHandler
import io.undertow.Handlers
import io.undertow.Handlers.path
import io.undertow.Undertow
import kotlinx.coroutines.experimental.newSingleThreadContext


val applicationSingleThreadContext = newSingleThreadContext("Coroutines Single Thread Pool")
val jsonParser = ObjectMapper()


fun main(args: Array<String>) {

    val server = Undertow.builder()
            .addHttpListener(8082, "127.0.0.1")
            .setHandler(path()
                    .addPrefixPath("/", Handlers.websocket(RootWebSocketHandler()))
            )
            .build()

    server.start()
}

