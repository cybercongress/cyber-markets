package fund.cyber.markets.model


import java.math.BigDecimal


enum class TradeType {
    SELL,
    BUY,
    UNKNOWN
}

data class Trade(

        //some markets get crazy id (ex: kraken - 1499515072.2199)
        val tradeId: String,
        val exchange: String,
        val timestamp: Long,
        val type: TradeType,
        val baseToken: String,
        val quoteToken: String,
        val baseAmount: BigDecimal,
        val quoteAmount: BigDecimal,
        val spotPrice: BigDecimal
)