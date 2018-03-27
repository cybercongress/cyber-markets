package fund.cyber.markets.rest.model

import java.math.BigDecimal

data class TokenModel(
        val fromsymbol: String,
        val tosymbol: String,
        val supply: BigDecimal?,
        val price: BigDecimal?,
        val volume: BigDecimal?,
        val priceChange: BigDecimal?
)