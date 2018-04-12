package fund.cyber.markets.api.common

import fund.cyber.markets.common.model.TokensPair

/**
 * @author mgergalov
 */
data class StreamApiResponseMessage (
        val type : String,
        val value : Any?
)

data class PairsByTokenBo (
        val token : String,
        val pairs : List<TokensPair>
)