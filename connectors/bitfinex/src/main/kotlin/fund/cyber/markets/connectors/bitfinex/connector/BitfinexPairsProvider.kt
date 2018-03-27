package fund.cyber.markets.connectors.bitfinex.connector

import fund.cyber.markets.connectors.common.PairsProvider
import fund.cyber.markets.model.TokensPairInitializer

class BitfinexPairsProvider: PairsProvider {

    private val pairsMap = mutableMapOf<String, TokensPairInitializer>(
            "ETHBTC" to TokensPairInitializer("ETH", "BTC")
    )


    suspend override fun getPairs(): Map<String, TokensPairInitializer> = pairsMap

}
