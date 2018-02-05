package fund.cyber.markets.rest.util

import com.fasterxml.jackson.databind.ObjectMapper
import fund.cyber.markets.model.Supply
import fund.cyber.markets.rest.configuration.AppContext
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
        val request = HttpGet(AppContext.CYBER_CHAINGEAR_API + "/api/tokens")
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
     * Returns the list of supplies using cryptocompare for tokeans from chaingear
     * @return a {@code List<Supply>}
     * @see {Supply}
     */
    fun getSupplies(): List<Supply> {
        val supplies = mutableListOf<Supply>()
        val client: HttpClient = HttpClientBuilder.create().build()
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
                        if (supply != null) {
                            supplies.add(Supply(token, BigDecimal.valueOf(supply)))
                        }
                    }

                }

        return supplies
    }

}

