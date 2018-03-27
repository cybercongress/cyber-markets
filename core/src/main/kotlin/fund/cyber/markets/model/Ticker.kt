package fund.cyber.markets.model

import java.math.BigDecimal
import java.util.*

data class Ticker(
        var pair: TokensPair = TokensPair("base", "quote"),
        var windowDuration: Long = -1L,
        var exchange: String = "exchange",
        var timestampTo: Date? = null,
        var timestampFrom: Date? = null,

        var avgPrice: BigDecimal = BigDecimal(-1L),
        var open: BigDecimal = BigDecimal.ZERO,
        var close: BigDecimal = BigDecimal.ZERO,

        var minPrice: BigDecimal = BigDecimal(Long.MAX_VALUE),
        var maxPrice: BigDecimal = BigDecimal(Long.MIN_VALUE),

        var baseAmount: BigDecimal = BigDecimal.ZERO,
        var quoteAmount: BigDecimal = BigDecimal.ZERO,

        var tradeCount: Long = 0
)