package fund.cyber.markets.model

/**
 * Exchange model elements.
 *
 * @author hleb.albau@gmail.com
 */

val bitfinex = "Bitfinex"
val poloniex = "Poloniex"


data class CurrencyPair(
        val baseCurrency: String,
        val counterCurrency: String
) {
    fun label(): String {
        return baseCurrency + "/" + counterCurrency
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