package fund.cyber.markets.model

import com.datastax.driver.mapping.annotations.Column
import com.datastax.driver.mapping.annotations.PartitionKey
import com.datastax.driver.mapping.annotations.Table
import java.math.BigDecimal

/**
 * Supply object
 * A variable value of type {@code BigDecimal} stores a value of a token circulating supply
 * A variable totalValue of type {@code BigDecimal} stores a value of a token total supply
 * A variable token of type {@code String} stores a token symbol
 */
@Table( keyspace = "markets", name = "supply",
        readConsistency = "QUORUM", writeConsistency = "QUORUM",
        caseSensitiveKeyspace = false, caseSensitiveTable = false)
data class TokenSupply(

        @PartitionKey(0)
        @Column(name = "tokenSymbol")
        val token: String,

        val value: BigDecimal,

        val totalValue: BigDecimal
)