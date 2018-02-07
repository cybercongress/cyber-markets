package fund.cyber.markets.model

import java.sql.Timestamp

data class TokenVolumeKey(
        val token: String,
        val windowDuration: Long,
        val timestamp: Timestamp
)