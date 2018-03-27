package fund.cyber.markets.connectors.poloniex.connector

import fund.cyber.markets.connectors.common.PairsProvider
import fund.cyber.markets.connectors.httpClient
import fund.cyber.markets.connectors.jsonParser
import fund.cyber.markets.connectors.common.await
import fund.cyber.markets.model.TokensPairInitializer
import okhttp3.Request


class PoloniexPairsProvider : PairsProvider {

    private val tickerRequest = Request.Builder().url("https://poloniex.com/public?command=returnTicker").build()!!

    suspend override fun getPairs(): Map<String, TokensPairInitializer> {
        val result = hashMapOf<String, TokensPairInitializer>()

        val response = httpClient.newCall(tickerRequest).await()
        val pairsTickers = jsonParser.readTree(response.body()?.string())

        pairsTickers.fields().forEach { pairTicker ->
            val tokensPair = TokensPairInitializer.fromLabel(pairTicker.key, "_")
            result.put(pairTicker.value["id"].asText(), TokensPairInitializer(tokensPair.pair.base, tokensPair.pair.quote))
        }

        return result
    }
}