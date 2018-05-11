package fund.cyber.markets.common.model

import java.math.BigDecimal
import java.util.*

data class Trade(
    val exchange: String,
    val pair: TokensPair,
    val type: TradeType,
    val timestamp: Date,
    val epochMinute: Long,
    val tradeId: String,
    val baseAmount: BigDecimal,
    val quoteAmount: BigDecimal,
    val price: BigDecimal
)

enum class TradeType {
    ASK,
    BID
}