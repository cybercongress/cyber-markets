package fund.cyber.markets.connectors.hitbtc

import com.fasterxml.jackson.databind.JsonNode
import fund.cyber.markets.connectors.common.PairsProvider
import fund.cyber.markets.connectors.httpClient
import fund.cyber.markets.connectors.jsonParser
import fund.cyber.markets.helpers.await
import fund.cyber.markets.model.TokensPair
import okhttp3.Request
import java.math.BigDecimal

class HitBtcPairsProvider: PairsProvider {

    val symbolsRequest = Request.Builder().url("https://api.hitbtc.com/api/1/public/symbols").build()!!

    suspend override fun getPairs(): Map<String, TokensPair> {
        val result = mutableMapOf<String, TokensPair>()
        val response = httpClient.newCall(symbolsRequest).await()
        jsonParser.readTree(response.body()?.string())["symbols"].toList().forEach { symbolInfo ->
            val tokensPair = hitBtcTokensPair(symbolInfo)
            result.put(tokensPair.symbol, tokensPair)
        }
        return result
    }

    private fun hitBtcTokensPair(symbolInfo: JsonNode): HitBtcTokensPair {
        val tokensPair = HitBtcTokensPair(
                base = symbolInfo["commodity"].asText(), quote = symbolInfo["currency"].asText(),
                symbol = symbolInfo["symbol"].asText(),
                lotSize = BigDecimal(symbolInfo["lot"].asText()),
                priceStep = BigDecimal(symbolInfo["step"].asText())
        )
        return tokensPair
    }
}
