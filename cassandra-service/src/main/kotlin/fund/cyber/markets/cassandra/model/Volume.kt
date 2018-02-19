package fund.cyber.markets.cassandra.model

import fund.cyber.markets.model.TokenVolume
import org.springframework.data.cassandra.core.cql.PrimaryKeyType
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn
import org.springframework.data.cassandra.core.mapping.Table
import java.math.BigDecimal
import java.util.*

@Table("volume")
data class CqlTokenVolume(
        @PrimaryKeyColumn(ordinal = 0, type = PrimaryKeyType.PARTITIONED, name = "tokenSymbol")
        val token: String,

        @PrimaryKeyColumn(ordinal = 1, type = PrimaryKeyType.PARTITIONED)
        val windowDuration: Long,

        @PrimaryKeyColumn(ordinal = 2, type = PrimaryKeyType.CLUSTERED)
        val exchange: String,

        val value: BigDecimal,

        val timestampFrom: Date,

        @PrimaryKeyColumn(ordinal = 3, type = PrimaryKeyType.CLUSTERED)
        val timestampTo: Date
) {
    constructor(tokenVolume: TokenVolume) : this(
            token = tokenVolume.token, windowDuration = tokenVolume.windowDuration,
            exchange = tokenVolume.exchange, value = tokenVolume.value,
            timestampFrom = tokenVolume.timestampFrom, timestampTo = tokenVolume.timestampTo
    )
}