package fund.cyber.markets.exchanges.hitbtc

import fund.cyber.markets.exchanges.common.ws.ReconnectableWsExchange
import fund.cyber.markets.helpers.await
import fund.cyber.markets.httpClient
import fund.cyber.markets.jsonParser
import fund.cyber.markets.model.TokensPair
import io.undertow.websockets.core.WebSocketChannel
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

class HitBtcExchange : ReconnectableWsExchange() {

    override val name = "HitBtc"
    override val wsAddress = "ws://api.hitbtc.com:80"

    private val channelSymbolForTokensPairs = hashMapOf<String, HitBtcTokensPair>()
    override val messageParser = HitBtcMessageParser(channelSymbolForTokensPairs)

    val symbolsRequest = Request.Builder().url("https://api.hitbtc.com/api/1/public/symbols").build()!!

    override suspend fun initializeMetadata() {

        val response = httpClient.newCall(symbolsRequest).await()
        jsonParser.readTree(response.body()?.string())["symbols"].toList().forEach { symbolInfo ->
            val tokensPair = HitBtcTokensPair(
                    base = symbolInfo["commodity"].asText(), quote = symbolInfo["currency"].asText(),
                    symbol = symbolInfo["symbol"].asText(),
                    lotSize = BigDecimal(symbolInfo["lot"].asText()),
                    priceStep = BigDecimal(symbolInfo["step"].asText())
            )
            channelSymbolForTokensPairs.put(tokensPair.symbol, tokensPair)
        }
    }

    override fun subscribeChannels(connection: WebSocketChannel) {
        //hit btc do not use channels abstraction
        //after subscribing, messages for all available pairs will be pushed
    }
}