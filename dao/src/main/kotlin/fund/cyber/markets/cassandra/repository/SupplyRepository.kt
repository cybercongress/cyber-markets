package fund.cyber.markets.cassandra.repository

import com.datastax.driver.core.Cluster
import com.datastax.driver.mapping.MappingManager
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.JdkFutureAdapters
import fund.cyber.markets.cassandra.MARKETS_KEYSPACE
import fund.cyber.markets.cassandra.PREFERRED_CONCURRENT_REQUEST_TO_SAVE_ENTITIES_LIST
import fund.cyber.markets.model.TokenSupply
import io.reactivex.Flowable

class SupplyRepository(cassandra: Cluster) {

    private val session = cassandra.connect(MARKETS_KEYSPACE)
    private val manager = MappingManager(session)
    private val supplyMapper by lazy { manager.mapper(TokenSupply::class.java) }

    fun save(tokenSupply: TokenSupply) {
        supplyMapper.save(tokenSupply)
    }

    fun saveAll(tokenSupplies: List<TokenSupply>) {
        Flowable.fromIterable(tokenSupplies)
                .buffer(PREFERRED_CONCURRENT_REQUEST_TO_SAVE_ENTITIES_LIST)
                .blockingForEach { entitiesChunk ->
                    val futures = entitiesChunk
                            .map { entity -> supplyMapper.saveAsync(entity) }
                            .map { future ->  JdkFutureAdapters.listenInPoolThread(future) }
                    Futures.allAsList(futures).get()
                }
    }

    fun get(token: String): TokenSupply? {
        return supplyMapper.get(token)
    }

}