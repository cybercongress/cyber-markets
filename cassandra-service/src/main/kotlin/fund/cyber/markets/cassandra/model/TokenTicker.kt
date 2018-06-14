package fund.cyber.markets.cassandra.model

import fund.cyber.markets.common.MILLIS_TO_DAYS
import fund.cyber.markets.common.convert
import fund.cyber.markets.common.model.TickerPrice
import fund.cyber.markets.common.model.TokenTicker
import org.springframework.data.cassandra.core.cql.PrimaryKeyType
import org.springframework.data.cassandra.core.mapping.Column
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

    @PrimaryKeyColumn(ordinal = 0, type = PrimaryKeyType.PARTITIONED, value = "tokenSymbol")
    val symbol: String,

    @PrimaryKeyColumn(ordinal = 1, type = PrimaryKeyType.PARTITIONED, value = "epochDay")
    val epochDay: Long,

    @PrimaryKeyColumn(ordinal = 2, type = PrimaryKeyType.CLUSTERED, value = "timestampFrom")
    var timestampFrom: Date,

    @Column("timestampTo")
    var timestampTo: Date,

    @PrimaryKeyColumn(ordinal = 3, type = PrimaryKeyType.CLUSTERED, value = "interval")
    val interval: Long,

    @Column("volume")
    val volume: CqlTickerVolume,

    @Column("baseVolume")
    val baseVolume: CqlTickerVolume,

    @Column("price")
    val price: Map<String, Map<String, CqlTickerPrice>>
) {
    constructor(tokenTicker: TokenTicker) : this(
        tokenTicker.symbol,
        tokenTicker.timestampFrom convert MILLIS_TO_DAYS,
        Date(tokenTicker.timestampFrom),
        Date(tokenTicker.timestampTo),
        tokenTicker.interval,
        CqlTickerVolume(tokenTicker.volume),
        CqlTickerVolume(tokenTicker.baseVolume),
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

data class CqlTickerVolume(
    val value: Map<String, Map<String, BigDecimal>>
)