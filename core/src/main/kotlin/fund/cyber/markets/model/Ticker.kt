package fund.cyber.markets.model

import com.datastax.driver.mapping.annotations.ClusteringColumn
import com.datastax.driver.mapping.annotations.Frozen
import com.datastax.driver.mapping.annotations.PartitionKey
import com.datastax.driver.mapping.annotations.Table
import java.math.BigDecimal
import java.util.*

@Table( keyspace = "markets", name = "ticker",
        readConsistency = "QUORUM", writeConsistency = "QUORUM",
        caseSensitiveKeyspace = false, caseSensitiveTable = false)
data class Ticker(
        @PartitionKey(0) @Frozen var pair: TokensPair = TokensPair("base", "quote"),
        @PartitionKey(1) var windowDuration: Long = -1L,
        @ClusteringColumn(0) var exchange: String = "exchange",
        @ClusteringColumn(1) var timestampTo: Date? = null,
        var timestampFrom: Date? = null,

        var avgPrice: BigDecimal = BigDecimal(-1L),
        var open: BigDecimal = BigDecimal.ZERO,
        var close: BigDecimal = BigDecimal.ZERO,

        var minPrice: BigDecimal = BigDecimal(Long.MAX_VALUE),
        var maxPrice: BigDecimal = BigDecimal(Long.MIN_VALUE),

        var baseAmount: BigDecimal = BigDecimal.ZERO,
        var quoteAmount: BigDecimal = BigDecimal.ZERO,

        var tradeCount: Long = 0
)