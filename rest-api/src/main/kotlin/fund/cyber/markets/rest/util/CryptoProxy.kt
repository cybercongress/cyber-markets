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


object CryptoProxy {

    private val mapper = ObjectMapper()

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

    fun getSupplies(): List<Supply> {
        val supplies = mutableListOf<Supply>()
        val client: HttpClient = HttpClientBuilder.create().build()
        val tokens = getTokens()

        Flowable
                .fromIterable(tokens)
                .buffer(20)
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

