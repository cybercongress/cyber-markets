package fund.cyber.markets.common.model

import java.math.BigDecimal

//value: exchange -> base token symbol -> BigDecimal value
data class TokenPrice(
    val symbol: String,
    val method: String,
    val timestampFrom: Long? = null,
    val timestampTo: Long,
    val values: MutableMap<String, MutableMap<String, BigDecimal>> = mutableMapOf()
)