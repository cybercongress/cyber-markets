package fund.cyber.markets.cassandra.model

import fund.cyber.markets.common.MILLIS_TO_HOURS
import fund.cyber.markets.common.convert
import fund.cyber.markets.common.model.OrderBook
import fund.cyber.markets.common.model.OrderSummary
import fund.cyber.markets.common.model.TokensPair
import org.springframework.data.cassandra.core.cql.Ordering
import org.springframework.data.cassandra.core.cql.PrimaryKeyType
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn
import org.springframework.data.cassandra.core.mapping.Table
import org.springframework.data.cassandra.core.mapping.UserDefinedType
import java.math.BigDecimal
import java.util.*

@Table("orderbook")
data class CqlOrderBook(

    @PrimaryKeyColumn(ordinal = 0, type = PrimaryKeyType.PARTITIONED, value = "exchange")
    val exchange: String,

    @PrimaryKeyColumn(ordinal = 1, type = PrimaryKeyType.PARTITIONED, value = "pair")
    val pair: CqlTokensPair,

    @PrimaryKeyColumn(ordinal = 2, type = PrimaryKeyType.PARTITIONED, value = "epochhour")
    val epochHour: Long,

    @PrimaryKeyColumn(ordinal = 0, type = PrimaryKeyType.CLUSTERED, value = "timestamp", ordering = Ordering.DESCENDING)
    val timestamp: Date,

    //@Frozen
    val bids: List<CqlOrderSummary>,

    //@Frozen
    val asks: List<CqlOrderSummary>
) {
    constructor(exchange: String, pair: TokensPair, orderBook: OrderBook): this(
        exchange = exchange,
        pair = CqlTokensPair(pair),
        epochHour = orderBook.timestamp convert MILLIS_TO_HOURS,
        timestamp = Date(orderBook.timestamp),
        bids = orderBook.bids.map { order -> CqlOrderSummary(order) },
        asks = orderBook.asks.map { order -> CqlOrderSummary(order) }
    )
}

@UserDefinedType("orderbook_order")
data class CqlOrderSummary(
    val type: String,
    val timestamp: Date,
    val amount: BigDecimal,
    val price: BigDecimal
) {
    constructor(order: OrderSummary): this(
        type = order.type.toString(),
        timestamp = Date(order.timestamp),
        amount = order.amount,
        price = order.price
    )
}