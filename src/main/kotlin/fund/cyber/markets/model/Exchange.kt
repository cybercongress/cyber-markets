package fund.cyber.markets.model

/**
 * Exchange model elements.
 *
 * @author hleb.albau@gmail.com
 */

data class CurrencyPair(
        val baseCurrency: String,
        val counterCurrency: String
)


data class ExchangeItems(
        val trades: MutableList<Trade> = ArrayList()
)