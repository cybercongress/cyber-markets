package fund.cyber.markets.ticker.configuration

import fund.cyber.markets.common.LAG_FROM_REAL_TIME_MIN
import fund.cyber.markets.common.LAG_FROM_REAL_TIME_MIN_DEFAULT
import fund.cyber.markets.common.MINUTES_TO_MILLIS
import fund.cyber.markets.common.WINDOW_DURATIONS_MIN
import fund.cyber.markets.common.WINDOW_DURATIONS_MIN_DEFAULT
import fund.cyber.markets.common.convert
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component

@Component
class TickersConfiguration(
    @Value("\${$WINDOW_DURATIONS_MIN:$WINDOW_DURATIONS_MIN_DEFAULT}")
    private val windowDurationsString: String,

    @Value("\${$LAG_FROM_REAL_TIME_MIN:$LAG_FROM_REAL_TIME_MIN_DEFAULT}")
    private val lag: Long
) {

    @Bean
    fun windowDurations(): Set<Long> = windowDurationsString
        .split(",")
        .map { duration -> duration.toLong() convert MINUTES_TO_MILLIS }
        .toSet()

    @Bean
    fun lagFromRealTime(): Long = lag convert MINUTES_TO_MILLIS

}