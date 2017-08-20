package fund.cyber.markets.connectors.poloniex

import fund.cyber.markets.connectors.common.PairsProvider
import fund.cyber.markets.connectors.httpClient
import fund.cyber.markets.connectors.jsonParser
import fund.cyber.markets.helpers.await
import fund.cyber.markets.model.TokensPair
import okhttp3.Request


class PoloniexPairsProvider: PairsProvider {

    private val tickerRequest = Request.Builder().url("https://poloniex.com/public?command=returnTicker").build()!!

    suspend override fun getPairs(): Map<String, TokensPair> {
        val result = hashMapOf<String, TokensPair>()

        val response = httpClient.newCall(tickerRequest).await()
        val pairsTickers = jsonParser.readTree(response.body()?.string())

        pairsTickers.fields().forEach { pairTicker ->
            result.put(pairTicker.value["id"].asText(), TokensPair.fromLabel(pairTicker.key, "_"))
        }

        return result
    }
}