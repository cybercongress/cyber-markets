package fund.cyber.markets.connectors.common

import fund.cyber.markets.model.TokensPairInitializer

/**
 * Created by aalbov on 18.8.17.
 */
interface PairsProvider {
    suspend fun getPairs(): Map<String, TokensPairInitializer>
}
