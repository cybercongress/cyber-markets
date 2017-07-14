package fund.cyber.markets.model

/**
 * Exchange model elements.
 *
 * @author hleb.albau@gmail.com
 */

val bitfinex = "Bitfinex"
val poloniex = "Poloniex"


data class TokensPair(
        val base: String,
        val quote: String
) {
    fun label(): String {
        return base + "/" + quote
    }
}

/**
 *
 * Websocket model part
 *
 */

open class ExchangeMessage

data class ExchangeItemsReceivedMessage(
        val trades: MutableList<Trade> = ArrayList()
) : ExchangeMessage()