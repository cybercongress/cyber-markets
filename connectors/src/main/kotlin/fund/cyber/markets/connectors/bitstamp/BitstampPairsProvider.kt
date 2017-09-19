package fund.cyber.markets.connectors.bitstamp

import fund.cyber.markets.connectors.common.PairsProvider
import fund.cyber.markets.model.TokensPair

class BitstampPairsProvider: PairsProvider {

    private val pairsMap = mutableMapOf<String, TokensPair>(
            "btceur" to TokensPair("BTC", "EUR"),
            "eurusd" to TokensPair("EUR", "USD"),
            "xrpusd" to TokensPair("XRP", "USD"),
            "xrpeur" to TokensPair("XRP", "EUR"),
            "xrpbtc" to TokensPair("XRP", "BTC"),
            "ltcusd" to TokensPair("LTC", "USD"),
            "ltceur" to TokensPair("LTC", "EUR"),
            "ltcbtc" to TokensPair("LTC", "BTC")
    )


    suspend override fun getPairs(): Map<String, TokensPair> = pairsMap
}