package fund.cyber.markets.common.model

data class OrderBook(
    val asks: MutableList<OrderMin>,
    val bids: MutableList<OrderMin>,
    val timestamp: Long
)