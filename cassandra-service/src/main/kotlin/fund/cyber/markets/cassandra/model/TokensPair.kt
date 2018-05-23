package fund.cyber.markets.cassandra.model

import fund.cyber.markets.common.model.TokensPair
import org.springframework.data.cassandra.core.mapping.UserDefinedType

@UserDefinedType("tokenpair")
data class CqlTokensPair(
    val base: String,
    val quote: String
) {
    constructor(pair: TokensPair): this(
        base = pair.base,
        quote = pair.quote
    )

    constructor(pair: String): this(
        pair.substringBefore("_"), pair.substringAfter("_")
    )
}