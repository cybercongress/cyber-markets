package fund.cyber.markets.common.model

data class OrderBook(
    val asks: MutableList<OrderSummary>,
    val bids: MutableList<OrderSummary>,
    val timestamp: Long
)