package fund.cyber.markets.common.rest.service

import fund.cyber.markets.common.model.OrderBook
import fund.cyber.markets.common.model.StringWrapper
import fund.cyber.markets.common.model.Token
import fund.cyber.markets.common.model.TokensPair
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.toFlux
import javax.annotation.PostConstruct

@Service
class ConnectorService(
    private val connectorApiUrls: List<String>
) {
    private val log = LoggerFactory.getLogger(javaClass)!!

    val connectorsMap = mutableMapOf<String, String>()

    @PostConstruct
    fun initConnectorsMap() {
        connectorApiUrls.toFlux().flatMap { url ->
            getExchanges(url).flatMap { exchange ->
                Mono.just(exchange.value to url)
            }
        }.collectList().block()?.toMap(connectorsMap)

        log.info("Connectors count: ${connectorsMap.values.toSet().size}. Exchanges count: ${connectorsMap.size}")
        log.debug("Connectors map: $connectorsMap")
    }

    private fun getExchanges(connectorApiUrl: String): Flux<StringWrapper> {
        val client = WebClient.create(connectorApiUrl)

        try {
            return client
                .get()
                .uri(EXCHANGE_LIST)
                .accept(APPLICATION_JSON)
                .retrieve()
                .bodyToFlux(StringWrapper::class.java)
        } catch (e: WebClientResponseException) {
            log.error("Cannot get list of connected exchanges from $connectorApiUrl. Status code: ${e.statusCode}")
        }

        return Flux.empty()
    }

    fun getExchanges(): Flux<String> {
        return connectorApiUrls.toFlux().flatMap { url ->
            getExchanges(url).map { stringWrapper -> stringWrapper.value }
        }
    }

    fun getTokensPairsByExchange(exchange: String): Flux<TokensPair> {
        val apiUrl = connectorsMap[exchange]
        if (apiUrl != null) {

            try {
                val client = WebClient.create(apiUrl)

                return client
                    .get()
                    .uri(EXCHANGE_PAIRS, mutableMapOf("exchangeName" to exchange))
                    .accept(APPLICATION_JSON)
                    .retrieve()
                    .bodyToFlux(TokensPair::class.java)
            } catch (e: WebClientResponseException) {
                log.error("Cannot get tokens pairs for $exchange exchange. Status code: {}", e.rawStatusCode)
            }

        } else {
            log.warn("Unknown exchange: $exchange")
        }

        return Flux.empty()
    }

    fun getOrderBook(exchange: String, pair: TokensPair): Mono<OrderBook> {
        val apiUrl = connectorsMap[exchange]

        if (apiUrl != null) {

            try {
                val client = WebClient.create(apiUrl)
                val orderBookUri = UriComponentsBuilder.fromUriString(ORDER_BOOK)
                    .queryParam("exchange", exchange)
                    .queryParam("pair", pair.pairString())
                    .toUriString()

                return client
                    .get()
                    .uri(orderBookUri)
                    .accept(APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(OrderBook::class.java)
            } catch (e: WebClientResponseException) {
                log.error("Cannot get order book from $exchange for pair: $pair. Response status code: {}", e.rawStatusCode)
            }

        } else {
            log.warn("Unknown exchange: $exchange")
        }

        return Mono.empty()
    }

    fun getTokens(): Flux<Token> {

        return connectorApiUrls.toFlux().flatMap { apiUrl ->
            WebClient
                .create(apiUrl)
                .get()
                .uri(TOKEN_LIST)
                .retrieve()
                .bodyToFlux(Token::class.java)
        }
    }

    fun getTokensByExchange(exchange: String): Flux<Token> {
        val apiUrl = connectorsMap[exchange]

        if (apiUrl != null) {
            try {
                return WebClient.create(apiUrl)
                    .get()
                    .uri(TOKEN_LIST_BY_EXCHANGE, exchange)
                    .accept(APPLICATION_JSON)
                    .retrieve()
                    .bodyToFlux(Token::class.java)
            } catch (e: WebClientResponseException) {
                log.error("Cannot get token list from $exchange. Response status code: {}", e.rawStatusCode)
            }

        } else {
            log.warn("Unknown exchange: $exchange")
        }

        return Flux.empty()
    }

    fun getTokensCount(): Mono<Long> {

        return connectorApiUrls.toFlux().flatMap { apiUrl ->
            WebClient
                .create(apiUrl)
                .get()
                .uri(TOKENS_COUNT)
                .retrieve()
                .bodyToMono(Long::class.java)
        }
            .collectList()
            .map {
                it.sum()
            }
    }

    fun getTokensCountByExchange(exchange: String): Mono<Long> {
        val apiUrl = connectorsMap[exchange]

        if (apiUrl != null) {
            try {
                return WebClient.create(apiUrl)
                    .get()
                    .uri(TOKENS_COUNT_BY_EXCHANGE, exchange)
                    .accept(APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(Long::class.java)
            } catch (e: WebClientResponseException) {
                log.error("Cannot get token count from $exchange. Response status code: {}", e.rawStatusCode)
            }

        } else {
            log.warn("Unknown exchange: $exchange")
        }

        return Mono.empty()
    }

}