package fund.cyber.markets.rest.model

import java.math.BigDecimal

data class HistoEntity(
        val Response: String,
        val Data: MutableList<TickerData>,
        val TimeTo: Long,
        val TimeFrom: Long,
        val ConversionType: ConversionType
)

data class TickerData(
        val time: Long,
        val open: BigDecimal,
        val close: BigDecimal,
        val high: BigDecimal,
        val low: BigDecimal,
        val volumeFrom: BigDecimal?,
        val volumeTo: BigDecimal?
)

data class ConversionType(
        val type: String,
        val conversionSymbol: String
)