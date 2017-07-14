package fund.cyber.markets.model


import java.math.BigDecimal
import java.time.Instant


enum class TradeType {
    SELL,
    BUY,
    UNKNOWN
}

data class Trade(

        //some markets get crazy id (ex: kraken - 1499515072.2199)
        val id: String,
        val exchange: String,
        val timestamp: Long,
        val type: TradeType,
        val currencyPair: CurrencyPair,

        val baseAmount: BigDecimal,
        val counterAmount: BigDecimal,
        val rate: BigDecimal
)