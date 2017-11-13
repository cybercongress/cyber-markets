package fund.cyber.markets.tickers.model

import fund.cyber.markets.dto.TokensPair
import java.sql.Timestamp

data class WindowKey(
        val exchange: String,
        val tokensPair: TokensPair,
        val windowDuration: Long,
        val timestamp: Timestamp
) {
    constructor(tokensPair: TokensPair, windowDuration: Long, timestamp: Timestamp) : this("ALL", tokensPair, windowDuration, timestamp)
}