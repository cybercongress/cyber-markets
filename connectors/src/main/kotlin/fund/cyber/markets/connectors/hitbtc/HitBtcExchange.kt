package fund.cyber.markets.connectors.hitbtc

import com.fasterxml.jackson.databind.JsonNode
import fund.cyber.markets.connectors.common.ExchangeType
import fund.cyber.markets.connectors.common.ws.ReconnectableWsConnector
import fund.cyber.markets.connectors.common.ws.WsCommonExchange
import fund.cyber.markets.connectors.common.ws.WsConnector
import fund.cyber.markets.helpers.await
import fund.cyber.markets.connectors.httpClient
import fund.cyber.markets.connectors.jsonParser
import fund.cyber.markets.model.TokensPair
import okhttp3.Request
import java.math.BigDecimal


// pairs of currency and tokens
class HitBtcTokensPair(
        val symbol: String,
        val lotSize: BigDecimal,
        val priceStep: BigDecimal,
        base: String,
        quote: String
) : TokensPair(base, quote)

class HitBtcExchange(type: ExchangeType) : WsCommonExchange(type) {

    override val name = "HitBtc"
    override val wsAddress = "ws://api.hitbtc.com:80"
    override val connector: WsConnector = ReconnectableWsConnector(wsAddress, name)
    override val reconnectable: Boolean
        get() = type == ExchangeType.ORDERS

    private val channelSymbolForTokensPairs = hashMapOf<String, HitBtcTokensPair>()
    override val messageParser = HitBtcMessageParser(channelSymbolForTokensPairs)

    val symbolsRequest = Request.Builder().url("https://api.hitbtc.com/api/1/public/symbols").build()!!

    override suspend fun updatePairs(): List<String> {
        val response = httpClient.newCall(symbolsRequest).await()
        jsonParser.readTree(response.body()?.string())["symbols"].toList().forEach { symbolInfo ->
            val tokensPair = hitBtcTokensPair(symbolInfo)
            channelSymbolForTokensPairs.putIfAbsent(tokensPair.symbol, tokensPair)
        }
        return emptyList<String>()
    }

    suspend override fun initMetadata() {
        val response = httpClient.newCall(symbolsRequest).await()
        jsonParser.readTree(response.body()?.string())["symbols"].toList().forEach { symbolInfo ->
            val tokensPair = hitBtcTokensPair(symbolInfo)
            channelSymbolForTokensPairs.put(tokensPair.symbol, tokensPair)
        }
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

    override fun getSubscribeMessage(pairSymbol: String): String = ""
    override fun getPairsToSubscribe(): Collection<String> = emptyList()
}