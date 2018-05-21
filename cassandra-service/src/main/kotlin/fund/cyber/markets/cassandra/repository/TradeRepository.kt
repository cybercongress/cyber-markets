package fund.cyber.markets.cassandra.repository

import com.datastax.driver.core.ConsistencyLevel
import fund.cyber.markets.cassandra.model.CqlTokensPair
import fund.cyber.markets.cassandra.model.CqlTrade
import org.springframework.data.cassandra.core.mapping.MapId
import org.springframework.data.cassandra.repository.Consistency
import org.springframework.data.cassandra.repository.Query
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface TradeRepository : ReactiveCassandraRepository<CqlTrade, MapId> {

    @Consistency(value = ConsistencyLevel.LOCAL_QUORUM)
    @Query("""
        SELECT * FROM markets.trade
        WHERE exchange=:exchange AND pair=:pair AND epochMinute=:epochMinute""")
    fun find(@Param("exchange") exchange: String,
             @Param("pair") pair: CqlTokensPair,
             @Param("epochMinute") epochMinute: Long): Flux<CqlTrade>
}