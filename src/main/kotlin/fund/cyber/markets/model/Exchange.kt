package fund.cyber.markets.model

/**
 * Exchange model elements.
 *
 * @author hleb.albau@gmail.com
 */

val bitfinex = "Bitfinex"
val poloniex = "Poloniex"
val hitbtc = "HitBtc"


open class TokensPair(
        val base: String,
        val quote: String
) {
    fun label(): String {
        return base + "/" + quote
    }
}

open class ExchangeMetadata(
        val exchange: String,
        val wsAddress: String
)