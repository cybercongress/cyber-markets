package fund.cyber.markets.common.model

/**
 * @author mgergalov
 */
data class TokensPair(
    val base: String,
    val quote: String
) {
    constructor(pair: String) : this(
        pair.substringBefore("_"), pair.substringAfter("_")
    )

    fun pairString(): String {
        return base + "_" + quote
    }
}