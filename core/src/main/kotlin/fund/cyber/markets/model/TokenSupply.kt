package fund.cyber.markets.model

import java.math.BigDecimal

/**
 * Supply object
 * A variable value of type {@code BigDecimal} stores a value of a token circulating supply
 * A variable totalValue of type {@code BigDecimal} stores a value of a token total supply
 * A variable token of type {@code String} stores a token symbol
 */
data class TokenSupply(
        val token: String,
        val value: BigDecimal,
        val totalValue: BigDecimal
)