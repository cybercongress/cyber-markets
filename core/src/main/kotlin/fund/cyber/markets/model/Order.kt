package fund.cyber.markets.model


import java.math.BigDecimal

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

enum class OrderType {
    SELL,
    BUY,
    UNKNOWN
}