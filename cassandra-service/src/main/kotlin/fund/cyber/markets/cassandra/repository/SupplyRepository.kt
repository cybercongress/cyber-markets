package fund.cyber.markets.cassandra.repository

import com.datastax.driver.core.Cluster
import com.datastax.driver.mapping.MappingManager
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.JdkFutureAdapters
import fund.cyber.markets.cassandra.configuration.MARKETS_KEYSPACE
import fund.cyber.markets.cassandra.configuration.PREFERRED_CONCURRENT_REQUEST_TO_SAVE_ENTITIES_LIST
import fund.cyber.markets.cassandra.model.CqlTokenSupply
import io.reactivex.Flowable

class SupplyRepository(cassandra: Cluster) {

    private val session = cassandra.connect(MARKETS_KEYSPACE)
    private val manager = MappingManager(session)
    private val supplyMapper by lazy { manager.mapper(CqlTokenSupply::class.java) }

    fun save(tokenSupply: CqlTokenSupply) {
        supplyMapper.save(tokenSupply)
    }

    fun saveAll(tokenSupplies: List<CqlTokenSupply>) {
        Flowable.fromIterable(tokenSupplies)
                .buffer(PREFERRED_CONCURRENT_REQUEST_TO_SAVE_ENTITIES_LIST)
                .blockingForEach { entitiesChunk ->
                    val futures = entitiesChunk
                            .map { entity -> supplyMapper.saveAsync(entity) }
                            .map { future ->  JdkFutureAdapters.listenInPoolThread(future) }
                    Futures.allAsList(futures).get()
                }
    }

    fun get(token: String): CqlTokenSupply? {
        return supplyMapper.get(token)
    }

}