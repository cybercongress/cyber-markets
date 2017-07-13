package fund.cyber.markets.model


import java.math.BigDecimal
import java.time.Instant


enum class TradeType(
        val label: String
) {
    SELL("Sell"),
    BUY("Buy"),
    UNKNOWN("Unknown")
}

data class Trade(

        //some markets get crazy id (ex: kraken - 1499515072.2199)
        val id: String,
        val exchange: String,
        val timestamp: Instant,
        val type: TradeType,
        val currencyPair: CurrencyPair,

        val quantity: BigDecimal,
        val rate: BigDecimal,
        val total: BigDecimal
)