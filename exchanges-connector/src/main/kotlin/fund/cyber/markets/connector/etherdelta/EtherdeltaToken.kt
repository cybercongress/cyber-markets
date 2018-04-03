package fund.cyber.markets.connector.etherdelta

import java.math.BigInteger

data class EtherdeltaToken(
        val symbol: String,
        val base: BigInteger,
        val decimals: Int
)