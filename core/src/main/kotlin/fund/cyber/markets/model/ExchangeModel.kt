package fund.cyber.markets.model


import java.math.BigDecimal


enum class TradeType {
    SELL,
    BUY,
    UNKNOWN
}

interface HasTopic {
    fun topic(): String
}

data class Trade (

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
) : HasTopic {
    private val TRADES_TOPIC_PREFIX = "TRADES"
    override fun topic(): String {
        return "$TRADES_TOPIC_PREFIX-${exchange}"
    }
}