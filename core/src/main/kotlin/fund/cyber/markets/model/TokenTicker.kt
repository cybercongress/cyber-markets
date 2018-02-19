package fund.cyber.markets.model

import java.math.BigDecimal

data class TokenTicker(
        val symbol: String,

        val timestampFrom: Long,
        val timestampTo: Long,
        val interval: Long,

        val price: MutableMap<String, MutableMap<String, TokenPrice>> = mutableMapOf(),
        val volume: MutableMap<String, MutableMap<String, BigDecimal>> = mutableMapOf(),
        val baseVolume: MutableMap<String, MutableMap<String, BigDecimal>> = mutableMapOf()
)