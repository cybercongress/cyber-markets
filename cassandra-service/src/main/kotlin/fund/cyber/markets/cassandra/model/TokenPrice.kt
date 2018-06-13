package fund.cyber.markets.cassandra.model

import fund.cyber.markets.common.MILLIS_TO_HOURS
import fund.cyber.markets.common.convert
import fund.cyber.markets.common.model.TokenPrice
import org.springframework.data.cassandra.core.cql.PrimaryKeyType
import org.springframework.data.cassandra.core.mapping.Column
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn
import org.springframework.data.cassandra.core.mapping.Table
import java.math.BigDecimal

@Table("token_price")
data class CqlTokenPrice(

    @PrimaryKeyColumn(ordinal = 0, type = PrimaryKeyType.PARTITIONED, value = "tokenSymbol")
    val symbol: String,
    val method: String,
    val timestampFrom: Long?,

    @PrimaryKeyColumn(ordinal = 2, type = PrimaryKeyType.CLUSTERED, value = "timestampto")
    val timestampTo: Long,

    @PrimaryKeyColumn(ordinal = 1, type = PrimaryKeyType.PARTITIONED, value = "epochhourto")
    val epochHourTo: Long,

    @Column("values")
    val values: Map<String, Map<String, BigDecimal>>
) {
    constructor(tokenPrice: TokenPrice) : this(
        symbol = tokenPrice.symbol,
        method = tokenPrice.method,
        timestampFrom = tokenPrice.timestampFrom,
        timestampTo = tokenPrice.timestampTo,
        epochHourTo = tokenPrice.timestampTo convert MILLIS_TO_HOURS,
        values = tokenPrice.values
    )
}