package fund.cyber.markets.connectors.bitstamp

import fund.cyber.markets.connectors.common.PairsProvider
import fund.cyber.markets.model.TokensPairInitializer

class BitstampPairsProvider: PairsProvider {

    private val pairsMap = mutableMapOf<String, TokensPairInitializer>(
            "btceur" to TokensPairInitializer("BTC", "EUR"),
            "eurusd" to TokensPairInitializer("EUR", "USD"),
            "xrpusd" to TokensPairInitializer("XRP", "USD"),
            "xrpeur" to TokensPairInitializer("XRP", "EUR"),
            "xrpbtc" to TokensPairInitializer("XRP", "BTC"),
            "ltcusd" to TokensPairInitializer("LTC", "USD"),
            "ltceur" to TokensPairInitializer("LTC", "EUR"),
            "ltcbtc" to TokensPairInitializer("LTC", "BTC")
    )


    suspend override fun getPairs(): Map<String, TokensPairInitializer> = pairsMap
}