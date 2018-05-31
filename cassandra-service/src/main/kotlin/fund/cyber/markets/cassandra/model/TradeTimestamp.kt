package fund.cyber.markets.cassandra.model

import org.springframework.data.cassandra.core.cql.PrimaryKeyType
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn
import org.springframework.data.cassandra.core.mapping.Table

const val LAST_PROCESSED_TIMESTAMP_ID =  "LAST_PROCESSED_TIMESTAMP_ID"

@Table("trade_last_timestamp")
class CqlTradeLastTimestamp(

    @PrimaryKeyColumn(ordinal = 0, type = PrimaryKeyType.PARTITIONED, value = "id")
    val id: String = LAST_PROCESSED_TIMESTAMP_ID,
    val value: Long
)