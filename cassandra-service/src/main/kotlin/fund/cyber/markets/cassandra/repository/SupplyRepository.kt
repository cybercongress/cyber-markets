package fund.cyber.markets.cassandra.repository

import fund.cyber.markets.cassandra.model.CqlTokenSupply
import org.springframework.data.cassandra.core.mapping.MapId
import org.springframework.data.repository.reactive.ReactiveCrudRepository

interface SupplyRepository : ReactiveCrudRepository<CqlTokenSupply, MapId>