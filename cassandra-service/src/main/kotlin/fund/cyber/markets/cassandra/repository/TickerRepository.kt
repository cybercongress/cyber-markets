package fund.cyber.markets.cassandra.repository

import com.datastax.driver.core.ConsistencyLevel
import fund.cyber.markets.cassandra.model.CqlTokenTicker
import org.springframework.data.cassandra.core.mapping.MapId
import org.springframework.data.cassandra.repository.Consistency
import org.springframework.data.cassandra.repository.Query
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import java.util.*

@Repository
interface TickerRepository : ReactiveCassandraRepository<CqlTokenTicker, MapId> {

    @Consistency(value = ConsistencyLevel.LOCAL_QUORUM)
    @Query("""
        SELECT * FROM markets.ticker
        WHERE tokenSymbol=:tokenSymbol AND epochDay=:epochDay AND interval=:interval
        AND timestampFrom>=:timestampFrom AND timestampFrom<:timestampTo""")
    fun find(@Param("tokenSymbol") tokenSymbol: String,
             @Param("epochDay") epochDay: Long,
             @Param("timestampFrom") timestampFrom: Date,
             @Param("timestampTo") timestampTo: Date,
             @Param("interval") interval: Long): Flux<CqlTokenTicker>

    @Consistency(value = ConsistencyLevel.LOCAL_QUORUM)
    @Query("""
        SELECT * FROM markets.ticker
        WHERE tokenSymbol=:tokenSymbol AND epochDay=:epochDay AND interval=:interval
        AND timestampFrom>=:timestampFrom LIMIT :limitValue""")
    fun find(@Param("tokenSymbol") tokenSymbol: String,
             @Param("epochDay") epochDay: Long,
             @Param("timestampFrom") timestampFrom: Date,
             @Param("interval") interval: Long,
             @Param("limitValue") limit: Long): Flux<CqlTokenTicker>
}