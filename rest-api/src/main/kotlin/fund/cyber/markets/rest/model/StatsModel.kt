package fund.cyber.markets.rest.model

import java.math.BigDecimal

data class StatsModel(
        val total_24h_volume_usd : BigDecimal,
        val total_24h_percent_change_usd: BigDecimal,
        val total_24h_volume_btc: BigDecimal,
        val total_24h_percent_change_btc: BigDecimal
)