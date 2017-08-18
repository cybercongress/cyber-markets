package fund.cyber.markets.connectors.poloniex

import fund.cyber.markets.connectors.common.ExchangeType
import fund.cyber.markets.connectors.common.ws.ReconnectableWsConnector
import fund.cyber.markets.connectors.common.ws.WsCommonExchange
import fund.cyber.markets.connectors.common.ws.WsConnector
import fund.cyber.markets.helpers.await
import fund.cyber.markets.connectors.httpClient
import fund.cyber.markets.connectors.jsonParser
import fund.cyber.markets.model.TokensPair
import okhttp3.Request

class PoloniexExchange(type: ExchangeType) : WsCommonExchange(type) {
    override val name: String ="Poloniex"
    override val wsAddress: String = "wss://api2.poloniex.com/"
    override val connector: WsConnector = ReconnectableWsConnector(wsAddress, name)
    override val reconnectable: Boolean
        get() = type == ExchangeType.ORDERS

    private val channelIdForTokensPairs = hashMapOf<Int, TokensPair>()
    override val messageParser = PoloniexMessageParser(channelIdForTokensPairs)


    val tickerRequest = Request.Builder().url("https://poloniex.com/public?command=returnTicker").build()!!

    override suspend fun initMetadata() {
        val response = httpClient.newCall(tickerRequest).await()
        val pairsTickers = jsonParser.readTree(response.body()?.string())

        pairsTickers.fields().forEach { pairTicker ->
            channelIdForTokensPairs.put(pairTicker.value["id"].asInt(), TokensPair.fromLabel(pairTicker.key, "_"))
        }
    }

    override suspend fun updatePairs(): List<String> {
        val newPairs = mutableListOf<String>()
        val response = httpClient.newCall(tickerRequest).await()
        val pairsTickers = jsonParser.readTree(response.body()?.string())

        pairsTickers.fields().forEach { pairTicker ->
            val pair = channelIdForTokensPairs.putIfAbsent(pairTicker.value["id"].asInt(), TokensPair.fromLabel(pairTicker.key, "_"))
            pair ?: newPairs.add(pairTicker.value["id"].asText());
        }
        return newPairs
    }

    override fun getPairsToSubscribe(): Collection<String> {
        return channelIdForTokensPairs.keys.map { it.toString() }
    }

    override fun getSubscribeMessage(pairSymbol: String): String = """{"command":"subscribe","channel":"$pairSymbol"}"""

}
