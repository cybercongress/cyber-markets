package fund.cyber.markets.tickers.model

import fund.cyber.markets.dto.TokensPair
import java.sql.Timestamp

class WindowKey(
        val tokensPair: TokensPair,
        val timestamp: Timestamp
)