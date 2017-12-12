package fund.cyber.markets.rest.model

import java.math.BigDecimal

data class PriceMultiFullModel(
        val raw: MutableMap<String, MutableMap<String, PriceMultiFullData>>
)

data class PriceMultiFullData(
        val market: String,
        val fromSymbol: String,
        val toSymbol: String,
        val price: BigDecimal,
        val lastUpdate: Long,
        val volume24hour: BigDecimal,
        val volume24hourTo: BigDecimal,
        val high24hour: BigDecimal,
        val low24hour: BigDecimal
)