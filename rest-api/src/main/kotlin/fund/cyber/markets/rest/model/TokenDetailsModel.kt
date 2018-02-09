package fund.cyber.markets.rest.model

import java.math.BigDecimal

data class TokenDetailsModel(
        val priceUsd: BigDecimal,
        val priceBtc: BigDecimal,
        val priceEth: BigDecimal,

        val capitalizationUsd: BigDecimal,
        val capitalizationBtc: BigDecimal,

        val volume24hUsd: BigDecimal,
        val volume24h: BigDecimal,

        val supplyTotal: BigDecimal,
        val supplyCirculating: BigDecimal
)