package fund.cyber.markets.cassandra.model

import fund.cyber.markets.common.model.Trade
import org.springframework.data.cassandra.core.cql.Ordering
import org.springframework.data.cassandra.core.cql.PrimaryKeyType
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn
import org.springframework.data.cassandra.core.mapping.Table
import java.math.BigDecimal
import java.util.*

@Table("trade")
data class CqlTrade(

    @PrimaryKeyColumn(ordinal = 0, type = PrimaryKeyType.PARTITIONED, value = "exchange")
    val exchange: String,

    @PrimaryKeyColumn(ordinal = 1, type = PrimaryKeyType.PARTITIONED, value = "pair")
    val pair: CqlTokensPair,
    val type: String,
    val timestamp: Date,

    @PrimaryKeyColumn(ordinal = 2, type = PrimaryKeyType.PARTITIONED, value = "epochminute")
    val epochMinute: Long,

    @PrimaryKeyColumn(ordinal = 3, type = PrimaryKeyType.CLUSTERED, value = "tradeid", ordering = Ordering.DESCENDING)
    val tradeId: String,
    val baseAmount: BigDecimal,
    val quoteAmount: BigDecimal,
    val price: BigDecimal
) {
    constructor(trade: Trade) : this(
        exchange = trade.exchange,
        pair = CqlTokensPair(trade.pair),
        type = trade.type.toString(),
        timestamp = Date(trade.timestamp),
        epochMinute = trade.epochMinute,
        tradeId = trade.tradeId,
        baseAmount = trade.baseAmount,
        quoteAmount = trade.quoteAmount,
        price = trade.price
    )
}

@Table("trade_temporary")
data class CqlTradeTemporary(

    @PrimaryKeyColumn(ordinal = 1, type = PrimaryKeyType.CLUSTERED, value = "exchange")
    val exchange: String,

    @PrimaryKeyColumn(ordinal = 2, type = PrimaryKeyType.CLUSTERED, value = "pair")
    val pair: CqlTokensPair,
    val type: String,
    val timestamp: Date,

    @PrimaryKeyColumn(ordinal = 0, type = PrimaryKeyType.PARTITIONED, value = "epochminute")
    val epochMinute: Long,

    @PrimaryKeyColumn(ordinal = 3, type = PrimaryKeyType.CLUSTERED, value = "tradeid", ordering = Ordering.DESCENDING)
    val tradeId: String,
    val baseAmount: BigDecimal,
    val quoteAmount: BigDecimal,
    val price: BigDecimal
) {
    constructor(trade: Trade) : this(
        exchange = trade.exchange,
        pair = CqlTokensPair(trade.pair),
        type = trade.type.toString(),
        timestamp = Date(trade.timestamp),
        epochMinute = trade.epochMinute,
        tradeId = trade.tradeId,
        baseAmount = trade.baseAmount,
        quoteAmount = trade.quoteAmount,
        price = trade.price
    )
}