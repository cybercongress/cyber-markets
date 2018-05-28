package fund.cyber.markets.cassandra.model

import fund.cyber.markets.common.model.TickerPrice
import fund.cyber.markets.common.model.TokenTicker
import org.springframework.data.cassandra.core.cql.PrimaryKeyType
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn
import org.springframework.data.cassandra.core.mapping.Table
import org.springframework.data.cassandra.core.mapping.UserDefinedType
import java.math.BigDecimal
import java.util.*

/**
 * TokenTicker class for cassandra
 *
 * @property price - map of  BCT_Symbol(Base Tokens) -> Exchange -> TickerPrice
 * @property volume - map of CT_Symbol -> Exchange -> Volume
 * @property baseVolume - map of BCT_Symbol -> exchange -> TotalVolume in BCT
 */

@Table("ticker")
data class CqlTokenTicker(

    @PrimaryKeyColumn(ordinal = 0, type = PrimaryKeyType.PARTITIONED, value = "tokensymbol")
    val symbol: String,

    @PrimaryKeyColumn(ordinal = 1, type = PrimaryKeyType.CLUSTERED, value = "timestampfrom")
    var timestampFrom: Date,
    var timestampTo: Date,

    @PrimaryKeyColumn(ordinal = 2, type = PrimaryKeyType.CLUSTERED, value = "interval")
    val interval: Long,

    //@Frozen
    val volume: Map<String, Map<String, BigDecimal>>,
    //@Frozen
    val baseVolume: Map<String, Map<String, BigDecimal>>,
    //@Frozen
    val price: Map<String, Map<String, CqlTickerPrice>>
) {
    constructor(tokenTicker: TokenTicker) : this(
        tokenTicker.symbol,
        Date(tokenTicker.timestampFrom),
        Date(tokenTicker.timestampTo),
        tokenTicker.interval, tokenTicker.volume,
        tokenTicker.baseVolume,
        prices(tokenTicker))
}

private fun prices(tokenTicker: TokenTicker) =
    tokenTicker.price.mapValues { (_, exchangeMap) ->
        exchangeMap.mapValues { (_, tickerPrice) -> CqlTickerPrice(tickerPrice) }
    }

@UserDefinedType("tickerprice")
data class CqlTickerPrice(
    var open: BigDecimal,
    var close: BigDecimal,
    var min: BigDecimal,
    var max: BigDecimal
) {
    constructor(tickerPrice: TickerPrice) : this(
        open = tickerPrice.open,
        close = tickerPrice.close,
        min = tickerPrice.min,
        max = tickerPrice.max
    )
}