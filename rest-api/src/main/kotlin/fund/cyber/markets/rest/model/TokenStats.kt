package fund.cyber.markets.rest.model

import java.math.BigDecimal

data class TokenStats(
        var system: String,
        var price_usd: BigDecimal,
        var price_bit: BigDecimal,
        var price_history: MutableList<BigDecimal>,
        var percent: BigDecimal
)