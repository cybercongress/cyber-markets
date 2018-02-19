package fund.cyber.markets.cassandra.model

import org.springframework.data.cassandra.core.cql.PrimaryKeyType
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn
import org.springframework.data.cassandra.core.mapping.Table
import java.math.BigDecimal

/**
 * Supply object for CQL queries
 * A variable value of type {@code BigDecimal} stores a value of a token circulating supply
 * A variable totalValue of type {@code BigDecimal} stores a value of a token total supply
 * A variable token of type {@code String} stores a token symbol
 */
@Table( "supply")
data class CqlTokenSupply(
        @PrimaryKeyColumn(ordinal = 0, type = PrimaryKeyType.PARTITIONED, name = "tokenSymbol")
        val token: String,
        val value: BigDecimal,
        val totalValue: BigDecimal
)