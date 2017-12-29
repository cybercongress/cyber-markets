package fund.cyber.markets.rest.model

import java.math.BigDecimal

data class HistoEntity(
        val response: String,
        val data: MutableList<TickerData>,
        val timeTo: Long,
        val timeFrom: Long,
        val conversionType: ConversionType
)

data class TickerData(
        val time: Long,
        val price: BigDecimal,
        val open: BigDecimal,
        val close: BigDecimal,
        val high: BigDecimal,
        val low: BigDecimal,
        val volumeFrom: BigDecimal,
        val volumeTo: BigDecimal
)

data class ConversionType(
        val type: String,
        val conversionSymbol: String
)