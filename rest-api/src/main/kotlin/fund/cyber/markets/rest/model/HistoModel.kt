package fund.cyber.markets.rest.model

import com.fasterxml.jackson.annotation.JsonProperty
import fund.cyber.markets.model.Ticker
import java.math.BigDecimal

data class HistoEntity(
        @get:JsonProperty("Response")
        val response: String,

        @get:JsonProperty("Data")
        val data: MutableList<TickerData>,

        @get:JsonProperty("TimeTo")
        val timeTo: Long,

        @get:JsonProperty("TimeFrom")
        val timeFrom: Long,

        @get:JsonProperty("ConversionType")
        val conversionType: ConversionType
)

data class TickerData(
        val time: Long,
        val open: BigDecimal,
        val close: BigDecimal,
        val high: BigDecimal,
        val low: BigDecimal,
        val volumeFrom: BigDecimal?,
        val volumeTo: BigDecimal?
) {
        constructor(ticker: Ticker, volumeBase: BigDecimal?, volumeQuote: BigDecimal?) : this(
                ticker.timestampTo?.time!! / 1000,
                ticker.open,
                ticker.close,
                ticker.maxPrice,
                ticker.minPrice,
                volumeBase,
                volumeQuote
        )

        constructor(time: Long, close: BigDecimal) : this(
                time,
                close,
                close,
                close,
                close,
                BigDecimal.ZERO,
                BigDecimal.ZERO
        )
}

data class ConversionType(
        val type: String,
        val conversionSymbol: String
)