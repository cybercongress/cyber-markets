package fund.cyber.markets.common.model


import java.math.BigDecimal
import java.util.*

data class OrderMin(
    val type: OrderType,
    val timestamp: Long,
    val amount: BigDecimal,
    val price: BigDecimal
)

data class Order(
    val exchange: String,
    val pair: TokensPair,
    val type: OrderType,
    val timestamp: Long,
    val epochHour: Long,
    val orderId: String?,
    val amount: BigDecimal,
    val price: BigDecimal
)

data class OrdersBatch(
    val exchange: String,
    val pair: TokensPair,
    val orders: List<Order> = ArrayList()
)

enum class OrderType {
    BID,
    ASK,
    UNKNOWN
}