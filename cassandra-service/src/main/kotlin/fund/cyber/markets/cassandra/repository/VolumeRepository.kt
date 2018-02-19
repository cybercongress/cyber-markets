package fund.cyber.markets.cassandra.repository

import fund.cyber.markets.cassandra.model.CqlTokenVolume
import org.springframework.data.cassandra.core.mapping.MapId
import org.springframework.data.repository.reactive.ReactiveCrudRepository

interface VolumeRepository : ReactiveCrudRepository<CqlTokenVolume, MapId>