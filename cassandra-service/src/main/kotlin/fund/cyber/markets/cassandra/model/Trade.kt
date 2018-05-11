package fund.cyber.markets.cassandra.model

import com.datastax.driver.mapping.annotations.ClusteringColumn
import com.datastax.driver.mapping.annotations.PartitionKey
import com.datastax.driver.mapping.annotations.Table
import fund.cyber.markets.common.model.Trade
import java.math.BigDecimal
import java.util.*

@Table(keyspace = "markets", name = "trade",
    readConsistency = "QUORUM", writeConsistency = "QUORUM",
    caseSensitiveKeyspace = false, caseSensitiveTable = false)
data class CqlTrade(
    @PartitionKey(0)
    val exchange: String,
    @PartitionKey(1)
    val pair: CqlTokensPair,
    val type: String,
    val timestamp: Date,
    @PartitionKey(2)
    val epochHour: Long,
    @ClusteringColumn(0)
    val tradeId: String,
    val baseAmount: BigDecimal,
    val quoteAmount: BigDecimal,
    val price: BigDecimal
) {
    constructor(trade: Trade) : this(
        exchange = trade.exchange,
        pair = CqlTokensPair(trade.pair),
        type = trade.type.toString(),
        timestamp = trade.timestamp,
        epochHour = trade.epochHour,
        tradeId = trade.tradeId,
        baseAmount = trade.baseAmount,
        quoteAmount = trade.quoteAmount,
        price = trade.price
    )
}