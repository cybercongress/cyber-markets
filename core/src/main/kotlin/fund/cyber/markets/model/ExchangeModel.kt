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