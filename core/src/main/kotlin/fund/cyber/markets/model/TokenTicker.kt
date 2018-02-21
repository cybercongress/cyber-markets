package fund.cyber.markets.model

import java.math.BigDecimal

/**
 * @property price - map of  BCT_Symbol(Base Tokens) -> Exchange -> TokenPrice
 * @property volume - map of CT_Symbol -> Exchange -> Volume
 * @property baseVolume - map of BCT_Symbol -> exchange -> TotalVolume in BCT
 */
data class TokenTicker(
        val symbol: String,

        var timestampFrom: Long,
        var timestampTo: Long,
        val interval: Long,

        val price: MutableMap<String, MutableMap<String, TokenPrice>> = mutableMapOf(),
        val volume: MutableMap<String, MutableMap<String, BigDecimal>> = mutableMapOf(),
        val baseVolume: MutableMap<String, MutableMap<String, BigDecimal>> = mutableMapOf()
)