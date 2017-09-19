package fund.cyber.markets.connectors.bitfinex

import fund.cyber.markets.connectors.common.PairsProvider
import fund.cyber.markets.model.TokensPair

class BitfinexPairsProvider: PairsProvider {

    private val pairsMap = mutableMapOf<String, TokensPair>(
            "ETHBTC" to TokensPair("ETH", "BTC")
    )


    suspend override fun getPairs(): Map<String, TokensPair> = pairsMap

}
