package fund.cyber.markets.ticker.configuration

import fund.cyber.markets.common.LAG_FROM_REAL_TIME_MIN
import fund.cyber.markets.common.LAG_FROM_REAL_TIME_MIN_DEFAULT
import fund.cyber.markets.common.MINUTES_TO_MILLIS
import fund.cyber.markets.common.WINDOW_INTERVALS_MIN
import fund.cyber.markets.common.WINDOW_INTERVALS_MIN_DEFAULT
import fund.cyber.markets.common.convert
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

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

}