package fund.cyber.markets.model


import java.math.BigDecimal


enum class TradeType {
    SELL,
    BUY,
    UNKNOWN
}

enum class OrderType {
    SELL,
    BUY,
    UNKNOWN
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
)

data class Order (
        val type: OrderType,
        val exchange: String,
        val baseToken: String,
        val quoteToken: String,
        val spotPrice: BigDecimal,
        val amount: BigDecimal
)

data class OrdersBatch (
        val baseToken: String,
        val exchange: String,
        val quoteToken: String,
        val orders: List<Order> = ArrayList()
)