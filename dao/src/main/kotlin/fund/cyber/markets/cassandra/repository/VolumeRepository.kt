package fund.cyber.markets.cassandra.repository

import com.datastax.driver.core.Cluster
import com.datastax.driver.mapping.MappingManager
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.JdkFutureAdapters
import fund.cyber.markets.cassandra.MARKETS_KEYSPACE
import fund.cyber.markets.cassandra.PREFERRED_CONCURRENT_REQUEST_TO_SAVE_ENTITIES_LIST
import fund.cyber.markets.cassandra.accessor.VolumeAccessor
import fund.cyber.markets.model.TokenVolume
import io.reactivex.Flowable

class VolumeRepository(cassandra: Cluster) {

    private val session = cassandra.connect(MARKETS_KEYSPACE)
    private val manager = MappingManager(session)
    private val volumeMapper by lazy { manager.mapper(TokenVolume::class.java) }
    private val volumeAccessor by lazy { manager.createAccessor(VolumeAccessor::class.java) }

    fun save(tokenVolume: TokenVolume) {
        volumeMapper.save(tokenVolume)
    }

    fun saveAll(tokenVolumes: List<TokenVolume>) {
        Flowable.fromIterable(tokenVolumes)
                .buffer(PREFERRED_CONCURRENT_REQUEST_TO_SAVE_ENTITIES_LIST)
                .blockingForEach { entitiesChunk ->
                    val futures = entitiesChunk
                            .map { entity -> volumeMapper.saveAsync(entity) }
                            .map { future ->  JdkFutureAdapters.listenInPoolThread(future) }
                    Futures.allAsList(futures).get()
                }
    }

}