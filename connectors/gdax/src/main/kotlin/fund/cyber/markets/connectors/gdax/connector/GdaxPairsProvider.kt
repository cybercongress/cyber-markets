package fund.cyber.markets.connectors.gdax.connector

import fund.cyber.markets.connectors.common.PairsProvider
import fund.cyber.markets.connectors.httpClient
import fund.cyber.markets.connectors.jsonParser
import fund.cyber.markets.helpers.await
import fund.cyber.markets.model.TokensPairInitializer
import okhttp3.Request

class GdaxPairsProvider : PairsProvider {

    val symbolsRequest = Request.Builder().url("https://api.gdax.com/products").build()!!

    suspend override fun getPairs(): Map<String, TokensPairInitializer> {
        val result = mutableMapOf<String, TokensPairInitializer>()
        val response = httpClient.newCall(symbolsRequest).await()
        //todo: add checkResponse isAvailableResource()
        val pairsTickers = jsonParser.readTree(response.body()?.string())
        pairsTickers.toList()?.forEach { pair ->
            result.put(pair.get("id").asText(),
                    TokensPairInitializer(pair.get("base_currency").asText(), pair.get("quote_currency").asText()))
        }
        return result
    }
}