package fund.cyber.markets.common.model

import java.math.BigDecimal

data class TokenPrice(
    val symbol: String,
    val timestampFrom: Long,
    val timestampTo: Long,
    val value: Map<String, BigDecimal>
)