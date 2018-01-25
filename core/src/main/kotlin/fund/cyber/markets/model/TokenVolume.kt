package fund.cyber.markets.model

import com.datastax.driver.mapping.annotations.ClusteringColumn
import com.datastax.driver.mapping.annotations.Column
import com.datastax.driver.mapping.annotations.PartitionKey
import com.datastax.driver.mapping.annotations.Table
import java.math.BigDecimal
import java.util.*

@Table( keyspace = "markets", name = "volume",
        readConsistency = "QUORUM", writeConsistency = "QUORUM",
        caseSensitiveKeyspace = false, caseSensitiveTable = false)
data class TokenVolume(
        @PartitionKey(0) @Column(name = "tokenSymbol") val token: String,
        @PartitionKey(1) val windowDuration: Long,
        @ClusteringColumn(0) val exchange: String,
        var value: BigDecimal,
        var timestampFrom: Date,
        @ClusteringColumn(1) var timestampTo: Date
)