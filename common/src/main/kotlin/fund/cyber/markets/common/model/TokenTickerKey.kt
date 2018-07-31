package fund.cyber.markets.common.model

import java.sql.Timestamp

data class TokenTickerKey(
    val symbol: String,
    val interval: Long,
    val timestamp: Timestamp
)