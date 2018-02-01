package fund.cyber.markets.cassandra.repository

import com.datastax.driver.core.Cluster
import com.datastax.driver.mapping.MappingManager
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.JdkFutureAdapters
import fund.cyber.markets.cassandra.MARKETS_KEYSPACE
import fund.cyber.markets.cassandra.PREFERRED_CONCURRENT_REQUEST_TO_SAVE_ENTITIES_LIST
import fund.cyber.markets.cassandra.accessor.VolumeAccessor
import fund.cyber.markets.common.Durations
import fund.cyber.markets.model.TokenVolume
import fund.cyber.markets.util.closestSmallerMultiply
import io.reactivex.Flowable
import java.math.BigDecimal
import java.util.*

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

    fun get(token: String, exchange: String, windowDuration: Long, timestamp: Long): TokenVolume? {
        return volumeMapper.get(token, windowDuration, exchange, Date(timestamp))
    }

    fun getVolume24h(token: String, exchange: String): TokenVolume? {
        val timestamp = closestSmallerMultiply(System.currentTimeMillis(), Durations.MINUTE) - Durations.DAY
        val volumes = volumeAccessor.getVolumes(token, Durations.MINUTE, exchange, Date(timestamp)).all()

        if (volumes.isEmpty()) {
            return null
        }

        val volume = TokenVolume(token, Durations.DAY, exchange, BigDecimal(0),
                volumes.first().timestampFrom,
                volumes.last().timestampTo
        )
        volumes.forEach { volume1m ->
            volume.value = volume.value.plus(volume1m.value)
        }

        return volume
    }

}