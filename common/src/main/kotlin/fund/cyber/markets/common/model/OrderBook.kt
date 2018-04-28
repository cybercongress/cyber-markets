package fund.cyber.markets.common.model

data class OrderBook(
    val asks: MutableList<Order>,
    val bids: MutableList<Order>,
    val timestamp: Long
)