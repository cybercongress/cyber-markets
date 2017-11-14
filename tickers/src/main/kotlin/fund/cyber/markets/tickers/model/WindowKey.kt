package fund.cyber.markets.tickers.model

import fund.cyber.markets.dto.TokensPair
import java.sql.Timestamp

data class WindowKey(
        val tokensPair: TokensPair,
        val windowDuration: Long,
        val timestamp: Timestamp
)