package fund.cyber.markets.common.model

import java.sql.Timestamp

data class TokenTickerKey(
        val symbol: String,
        val windowDuration: Long,
        val timestamp: Timestamp
)