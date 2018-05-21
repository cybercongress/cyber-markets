package fund.cyber.markets.cassandra.repository

import com.datastax.driver.core.ConsistencyLevel
import fund.cyber.markets.cassandra.model.CqlOrderBook
import fund.cyber.markets.cassandra.model.CqlTokensPair
import org.springframework.data.cassandra.core.mapping.MapId
import org.springframework.data.cassandra.repository.Consistency
import org.springframework.data.cassandra.repository.Query
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.util.*

@Repository
interface OrderBookRepository : ReactiveCassandraRepository<CqlOrderBook, MapId> {

    @Consistency(value = ConsistencyLevel.LOCAL_QUORUM)
    @Query("""
        SELECT * FROM markets.orderbook
        WHERE exchange=:exchange AND pair=:pair AND epochHour=:epochHour AND timestamp<=:timestamp""")
    fun findLastByTimestamp(@Param("exchange") exchange: String,
                           @Param("pair") pair: CqlTokensPair,
                           @Param("epochHour") epochHour: Long,
                           @Param("timestamp") timestamp: Date): Mono<CqlOrderBook>

}