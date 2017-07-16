package fund.cyber.markets.hitbtc

import fund.cyber.markets.model.TokensPair
import java.math.BigDecimal

// pairs of currency and tokens
class HitBtcTokensPair(
        val symbol: String,
        val lotSize: BigDecimal,
        val priceStep: BigDecimal,
        base: String,
        quote: String
) : TokensPair(base, quote)