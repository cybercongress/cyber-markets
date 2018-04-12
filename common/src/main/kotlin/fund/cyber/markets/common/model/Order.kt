package fund.cyber.markets.common.model


import java.math.BigDecimal
import java.util.*

data class Order (
        val exchange: String,
        val pair: TokensPair,
        val type: OrderType,
        val timestamp: Date,
        val epochHour: Long,
        val orderId: String,
        val amount: BigDecimal,
        val price: BigDecimal
)

data class OrdersBatch (
        val exchange: String,
        val pair: TokensPair,
        val orders: List<Order> = ArrayList()
)

enum class OrderType {
    SELL,
    BUY,
    UNKNOWN
}