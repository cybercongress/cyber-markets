package fund.cyber.markets.connector.etherdelta

import java.math.BigInteger

/**
 * Etherdelta token definition class
 */
data class EtherdeltaToken(
        /**
         * The symbol of a token. For example: ETH
         */
        val symbol: String,

        /**
         * Divisor to convert the value of the number of tokens for display or calculations.
         * For example: if decimals == 3 then base will be 1000
         */
        val base: BigInteger,

        /**
         * The number of digits that come after the decimal place when displaying token values on-screen
         */
        val decimals: Int
)