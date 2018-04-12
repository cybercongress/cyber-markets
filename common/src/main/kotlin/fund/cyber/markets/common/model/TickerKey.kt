package fund.cyber.markets.common.model

import java.sql.Timestamp

data class TickerKey(
        val pair: TokensPair,
        val windowDuration: Long,
        val timestamp: Timestamp
)