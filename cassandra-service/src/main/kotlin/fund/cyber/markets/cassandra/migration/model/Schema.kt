package fund.cyber.markets.cassandra.migration.model

import org.springframework.data.cassandra.core.cql.PrimaryKeyType
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn
import org.springframework.data.cassandra.core.mapping.Table
import java.util.*

@Table("schema_version")
data class CqlSchemaVersion(
    @PrimaryKeyColumn("application_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED) val applicationId: String,
    @PrimaryKeyColumn(ordinal = 1, type = PrimaryKeyType.CLUSTERED) val id: String,
    val migration_hash: Int,
    val apply_time: Date
)
