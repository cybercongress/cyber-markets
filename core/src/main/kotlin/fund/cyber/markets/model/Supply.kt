package fund.cyber.markets.model

import com.datastax.driver.mapping.annotations.Column
import com.datastax.driver.mapping.annotations.PartitionKey
import com.datastax.driver.mapping.annotations.Table
import java.math.BigDecimal

@Table( keyspace = "markets", name = "supply",
        readConsistency = "QUORUM", writeConsistency = "QUORUM",
        caseSensitiveKeyspace = false, caseSensitiveTable = false)
data class Supply(
        @PartitionKey(0) @Column(name = "tokenSymbol") val token: String,
        val value: BigDecimal
)