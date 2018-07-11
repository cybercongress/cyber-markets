package fund.cyber.markets.common.rest

import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

fun <T> Mono<T>.asServerResponse() = asServerResponseAllowEmpty().switchIfEmpty(ServerResponse.notFound().build())

fun <T> Flux<T>.asServerResponse() = asServerResponseAllowEmpty().switchIfEmpty(ServerResponse.notFound().build())

fun <T> Mono<T>.asServerResponseAllowEmpty() = this.flatMap { obj -> ServerResponse.ok().body(BodyInserters.fromObject(obj)) }

fun <T> Flux<T>.asServerResponseAllowEmpty() = this.collectList().flatMap { obj ->
    when {
        obj.isNotEmpty() -> ServerResponse.ok().body(BodyInserters.fromObject(obj))
        else -> Mono.empty<ServerResponse>()
    }
}