package fund.cyber.markets.rest.util

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import fund.cyber.markets.model.TokenSupply
import fund.cyber.markets.rest.configuration.RestApiConfiguration
import io.reactivex.Flowable
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClientBuilder
import java.io.BufferedReader
import java.io.InputStreamReader
import java.math.BigDecimal

const val BUFFER_SIZE = 15

/**
 * Proxy object for chaingear and cryptocompare REST api
 */
object CryptoProxy {

    private val mapper = ObjectMapper()

    /**
     * Returns the set of tokens available in the chaingear
     * @return a {@code Set<String>}
     */
    fun getTokens(): Set<String> {
        val client: HttpClient = HttpClientBuilder.create().build()
        val request = HttpGet(RestApiConfiguration.chaingearHost + "/api/tokens")
        val response = client.execute(request)
        val bufferedReader = BufferedReader(InputStreamReader(response.entity.content))

        val tokens = mutableSetOf<String>()
        mapper.readTree(bufferedReader).map { item ->
            val symbol = item.get("token")?.get("symbol")?.asText()
            if (symbol != null && symbol.trim().isNotEmpty()) {
                tokens.add(symbol)
            }
        }

        return tokens
    }

    /**
     * Returns the {@code JsonNode} object with info about all tokens available in cryptocompare
     * @return a {@code JsonNode}
     */
    private fun getCoinListJsonNode(): JsonNode {
        val client: HttpClient = HttpClientBuilder.create().build()

        val coinList = "https://min-api.cryptocompare.com/data/all/coinlist"
        val request = HttpGet(coinList)
        val response = client.execute(request)
        val bufferedReader = BufferedReader(InputStreamReader(response.entity.content))

        return mapper.readTree(bufferedReader)
    }

    /**
     * Returns the list of supplies using cryptocompare for tokeans from chaingear
     * @return a {@code List<TokenSupply>}
     * @see {TokenSupply}
     */
    fun getSupplies(): List<TokenSupply> {
        val client: HttpClient = HttpClientBuilder.create().build()

        val supplies = mutableListOf<TokenSupply>()
        val coinListNode = getCoinListJsonNode()
        val tokens = getTokens()

        Flowable
                .fromIterable(tokens)
                .buffer(BUFFER_SIZE)
                .blockingForEach { tokensChunk ->

                    val symbols = tokensChunk.joinToString(",")
                    val url = "https://min-api.cryptocompare.com/data/pricemultifull?fsyms=$symbols&tsyms=$symbols"
                    val request = HttpGet(url)
                    val response = client.execute(request)

                    val bufferedReader = BufferedReader(InputStreamReader(response.entity.content))
                    val node = mapper.readTree(bufferedReader).get("RAW")

                    tokensChunk.forEach { token ->
                        val supply = node?.get(token)?.get(token)?.get("SUPPLY")?.longValue()
                        val totalSupply = coinListNode.get("Data")?.get(token)?.get("TotalCoinSupply")?.asLong() ?: 0L
                        if (supply != null) {
                            supplies.add(TokenSupply(token, BigDecimal.valueOf(supply), BigDecimal.valueOf(totalSupply)))
                        }
                    }

                }

        return supplies
    }

}

