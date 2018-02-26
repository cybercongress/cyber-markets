package fund.cyber.markets.cassandra.model

import com.datastax.driver.mapping.annotations.Column
import com.datastax.driver.mapping.annotations.PartitionKey
import java.math.BigDecimal

/**
 * Supply object for CQL queries
 * A variable value of type {@code BigDecimal} stores a value of a token circulating supply
 * A variable totalValue of type {@code BigDecimal} stores a value of a token total supply
 * A variable token of type {@code String} stores a token symbol
 */
@com.datastax.driver.mapping.annotations.Table( keyspace = "markets", name = "supply",
        readConsistency = "QUORUM", writeConsistency = "QUORUM",
        caseSensitiveKeyspace = false, caseSensitiveTable = false)
data class CqlTokenSupply(
        @PartitionKey(0) @Column(name = "tokensymbol")
        val token: String,
        val value: BigDecimal,
        val totalValue: BigDecimal
)