package fund.cyber.markets.common.model

import java.sql.Timestamp

data class TickerKey(
    val pair: TokensPair,
    val interval: Long,
    val timestamp: Timestamp
)