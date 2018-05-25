package fund.cyber.markets.cassandra.repository

import com.datastax.driver.core.ConsistencyLevel
import fund.cyber.markets.cassandra.model.CqlTokenTicker
import org.springframework.data.cassandra.core.mapping.MapId
import org.springframework.data.cassandra.repository.Consistency
import org.springframework.data.cassandra.repository.Query
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.util.*

@Repository
interface TickerRepository : ReactiveCassandraRepository<CqlTokenTicker, MapId> {

    @Consistency(value = ConsistencyLevel.LOCAL_QUORUM)
    @Query("""
        SELECT * FROM markets.ticker
        WHERE tokensymbol=:tokenSymbol AND timestampfrom=:timestampFrom AND interval=:interval""")
    fun find(@Param("tokenSymbol") tokenSymbol: String,
             @Param("timestampFrom") timestampFrom: Date,
             @Param("interval") interval: Long): Mono<CqlTokenTicker>
}