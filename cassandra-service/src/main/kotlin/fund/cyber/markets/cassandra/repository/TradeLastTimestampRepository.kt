package fund.cyber.markets.cassandra.repository

import com.datastax.driver.core.ConsistencyLevel
import fund.cyber.markets.cassandra.model.CqlTradeLastTimestamp
import fund.cyber.markets.cassandra.model.LAST_PROCESSED_TIMESTAMP_ID
import org.springframework.data.cassandra.core.mapping.MapId
import org.springframework.data.cassandra.repository.Consistency
import org.springframework.data.cassandra.repository.Query
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface TradeLastTimestampRepository : ReactiveCassandraRepository<CqlTradeLastTimestamp, MapId> {

    @Consistency(value = ConsistencyLevel.LOCAL_QUORUM)
    @Query("""SELECT * FROM markets.trade_last_timestamp WHERE id='$LAST_PROCESSED_TIMESTAMP_ID'""")
    fun findTradeLastTimestamp(): Mono<CqlTradeLastTimestamp>
    
}