package fund.cyber.markets.api.common

import fund.cyber.markets.dto.TokensPair

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