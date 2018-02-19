package fund.cyber.markets.cassandra.model

import fund.cyber.markets.model.Ticker
import fund.cyber.markets.model.TokensPair
import org.springframework.data.cassandra.core.cql.PrimaryKeyType
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn
import org.springframework.data.cassandra.core.mapping.Table
import org.springframework.data.cassandra.core.mapping.UserDefinedType
import java.math.BigDecimal
import java.util.*

@Table("ticker")
data class CqlTicker(
        @PrimaryKeyColumn(ordinal = 0, type = PrimaryKeyType.PARTITIONED) var pair: CqlTokenPair,
        @PrimaryKeyColumn(ordinal = 1, type = PrimaryKeyType.PARTITIONED) var windowDuration: Long,
        @PrimaryKeyColumn(ordinal = 2, type = PrimaryKeyType.CLUSTERED) var exchange: String,
        @PrimaryKeyColumn(ordinal = 3, type = PrimaryKeyType.CLUSTERED) var timestampTo: Date?,
        var timestampFrom: Date?,

        var avgPrice: BigDecimal,
        var open: BigDecimal,
        var close: BigDecimal,

        var minPrice: BigDecimal,
        var maxPrice: BigDecimal,

        var baseAmount: BigDecimal,
        var quoteAmount: BigDecimal,

        var tradeCount: Long = 0
) {
    constructor(ticker: Ticker) : this(
            pair = CqlTokenPair(ticker.pair), windowDuration = ticker.windowDuration, exchange = ticker.exchange,
            timestampFrom = ticker.timestampFrom, timestampTo = ticker.timestampTo, avgPrice = ticker.avgPrice,
            close = ticker.close, open = ticker.open, minPrice = ticker.minPrice, maxPrice = ticker.maxPrice,
            baseAmount = ticker.baseAmount, quoteAmount = ticker.quoteAmount, tradeCount = ticker.tradeCount
    )
}

@UserDefinedType("tokenpair")
data class CqlTokenPair(
        val base: String,
        val quote: String
) {
    constructor(pair: TokensPair) : this(
            base = pair.base, quote = pair.quote
    )
}