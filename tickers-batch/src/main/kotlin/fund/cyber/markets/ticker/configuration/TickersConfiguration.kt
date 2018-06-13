package fund.cyber.markets.ticker.configuration

import fund.cyber.markets.cassandra.model.CqlTokenTicker
import fund.cyber.markets.common.LAG_FROM_REAL_TIME_MIN
import fund.cyber.markets.common.LAG_FROM_REAL_TIME_MIN_DEFAULT
import fund.cyber.markets.common.MINUTES_TO_MILLIS
import fund.cyber.markets.common.WINDOW_INTERVALS_MIN
import fund.cyber.markets.common.WINDOW_INTERVALS_MIN_DEFAULT
import fund.cyber.markets.common.convert
import fund.cyber.markets.ticker.service.TickerCacheKey
import org.ehcache.Cache
import org.ehcache.CacheManager
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.CacheManagerBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.ehcache.config.units.MemoryUnit
import org.ehcache.core.spi.service.StatisticsService
import org.ehcache.impl.internal.statistics.DefaultStatisticsService
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

const val TICKERS_CACHE_NAME = "tickers"
const val TICKERS_CACHE_SIZE_GB = 5L

@Configuration
class TickersConfiguration(
    @Value("\${$WINDOW_INTERVALS_MIN:$WINDOW_INTERVALS_MIN_DEFAULT}")
    private val windowIntervalsString: String,

    @Value("\${$LAG_FROM_REAL_TIME_MIN:$LAG_FROM_REAL_TIME_MIN_DEFAULT}")
    private val lag: Long
) {

    @Bean
    fun windowIntervals(): Set<Long> = windowIntervalsString
        .split(",")
        .map { duration -> duration.toLong() convert MINUTES_TO_MILLIS }
        .toSet()

    @Bean
    fun lagFromRealTime(): Long = lag convert MINUTES_TO_MILLIS

    @Bean
    fun tickerCache(cacheManager: CacheManager): Cache<TickerCacheKey, MutableList<CqlTokenTicker>> {
        return cacheManager.getCache(TICKERS_CACHE_NAME, TickerCacheKey::class.java, MutableList::class.java as Class<MutableList<CqlTokenTicker>>)
    }

    @Bean
    fun cacheStatisticsService() = DefaultStatisticsService()

    @Bean
    fun cacheManager(cacheStatisticsService: StatisticsService): CacheManager {

        return CacheManagerBuilder.newCacheManagerBuilder()
            .withCache(TICKERS_CACHE_NAME,
                CacheConfigurationBuilder.newCacheConfigurationBuilder(
                    TickerCacheKey::class.java,
                    MutableList::class.java as Class<MutableList<CqlTokenTicker>>,
                    ResourcePoolsBuilder.newResourcePoolsBuilder().heap(TICKERS_CACHE_SIZE_GB, MemoryUnit.GB)
                )
            )
            .using(cacheStatisticsService)
            .build(true)
    }

}