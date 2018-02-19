package fund.cyber.markets.cassandra.repository

import fund.cyber.markets.cassandra.model.CqlTicker
import org.springframework.data.cassandra.core.mapping.MapId
import org.springframework.data.repository.reactive.ReactiveCrudRepository

interface TickerRepository : ReactiveCrudRepository<CqlTicker, MapId>