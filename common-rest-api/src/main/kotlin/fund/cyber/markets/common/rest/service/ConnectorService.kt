package fund.cyber.markets.common.rest.service

import fund.cyber.markets.common.model.OrderBook
import fund.cyber.markets.common.model.TokensPair
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Mono
import javax.annotation.PostConstruct

const val ORDERBOOK_PATH = "/orderbook"
const val EXCHANGES_SET_PATH = "/exchanges"
const val EXCHANGE_TOKENS_PAIRS_PATH = "/exchange/{exchangeName}/pairs"

@Service
class ConnectorService {
    private val log = LoggerFactory.getLogger(javaClass)!!

    @Autowired
    private lateinit var connectorApiUrls: List<String>

    private val restTemplate = RestTemplate()
    val connectorsMap = mutableMapOf<String, String>()

    @PostConstruct
    fun initConnectorsMap() {
        connectorApiUrls.forEach { url ->
            getExchanges(url)?.forEach { exchange ->
                connectorsMap[exchange] = url
            }
        }
    }

    fun getExchanges(connectorApiUrl: String): Set<String>? {
        val requestUri = connectorApiUrl + EXCHANGES_SET_PATH

        var exchanges: Array<String>? = null
        try {
            exchanges = restTemplate.getForObject<Array<String>>(requestUri, Array<String>::class.java)
        } catch (e: HttpClientErrorException) {
            log.error("Cannot get list of connected exchanges from $connectorApiUrl", e)
        }

        return exchanges?.toSet()
    }

    fun getExchanges(): Mono<Set<String>> {
        return Mono.create { monoSink ->
            val exchanges = mutableSetOf<String>()

            connectorApiUrls.forEach { url ->
                val connectorExchanges = getExchanges(url)
                if (connectorExchanges != null) {
                    exchanges.addAll(connectorExchanges)
                }
            }

            monoSink.success(exchanges)
        }
    }

    fun getTokensPairsByExchange(exchange: String): Mono<Set<TokensPair>> {

        return Mono.create<Set<TokensPair>> { monoSink ->
            val apiUrl = connectorsMap[exchange]

            if (apiUrl != null) {
                val requestUri = apiUrl + EXCHANGE_TOKENS_PAIRS_PATH
                val parameters = mutableMapOf<String, String>().apply {
                    put("exchangeName", exchange)
                }

                try {
                    val pairs = restTemplate.getForObject<Array<TokensPair>>(requestUri, Array<TokensPair>::class.java, parameters)
                    monoSink.success(pairs?.toSet())
                } catch (e: HttpClientErrorException) {
                    log.error("Cannot get tokens pairs for $exchange exchange. Status code: {}", e.rawStatusCode)
                    monoSink.error(e)
                }
            } else {
                monoSink.success()
            }
        }

    }

    fun getOrderBook(exchange: String, pair: TokensPair): Mono<OrderBook> {

        return Mono.create<OrderBook> { monoSink ->
            val apiUrl = connectorsMap[exchange]

            if (apiUrl != null) {
                val requestUri = apiUrl + ORDERBOOK_PATH

                val builder = UriComponentsBuilder.fromUriString(requestUri)
                    .queryParam("exchange", exchange)
                    .queryParam("pair", pair.pairString())

                try {
                    val orderBook = restTemplate.getForObject<OrderBook>(builder.toUriString(), OrderBook::class.java)
                    monoSink.success(orderBook)
                } catch (e: HttpClientErrorException) {
                    log.error("Cannot get order book from $exchange and pair: $pair. Response status code: {}", e.rawStatusCode)
                    monoSink.error(e)
                }
            } else {
                monoSink.success()
            }
        }

    }

}