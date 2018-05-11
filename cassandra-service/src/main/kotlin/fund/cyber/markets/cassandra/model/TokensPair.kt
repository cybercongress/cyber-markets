package fund.cyber.markets.cassandra.model

import com.datastax.driver.mapping.annotations.UDT
import fund.cyber.markets.common.model.TokensPair

@UDT(name = "tokenpair")
data class CqlTokensPair(
    val base: String,
    val quote: String
) {
    constructor(pair: TokensPair): this(
        base = pair.base,
        quote = pair.quote
    )
}