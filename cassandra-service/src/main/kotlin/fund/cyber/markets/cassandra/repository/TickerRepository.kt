package fund.cyber.markets.cassandra.repository

import fund.cyber.markets.cassandra.model.CqlTokenTicker
import org.springframework.data.cassandra.core.mapping.MapId
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository
import org.springframework.stereotype.Repository

@Repository
interface TickerRepository : ReactiveCassandraRepository<CqlTokenTicker, MapId>