package fund.cyber.markets.ticker.configuration

import fund.cyber.markets.common.ALLOW_NOT_CLOSED_WINDOWS
import fund.cyber.markets.common.ALLOW_NOT_CLOSED_WINDOWS_DEFAULT
import fund.cyber.markets.common.Intervals
import fund.cyber.markets.common.SECONDS_TO_MILLIS
import fund.cyber.markets.common.WINDOW_HOP_SEC
import fund.cyber.markets.common.WINDOW_HOP_SEC_DEFAULT
import fund.cyber.markets.common.WINDOW_INTERVALS_MIN
import fund.cyber.markets.common.WINDOW_INTERVALS_MIN_DEFAULT
import fund.cyber.markets.common.convert
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

private const val POLL_TIMEOUT_COEFFICIENT: Double = 0.5

@Component
class TickersConfiguration(
        @Value("\${$WINDOW_HOP_SEC:$WINDOW_HOP_SEC_DEFAULT}")
        private val windowHopSec: Long,

        @Value("\${$ALLOW_NOT_CLOSED_WINDOWS:$ALLOW_NOT_CLOSED_WINDOWS_DEFAULT}")
        private val allowNotClosedWindows: Boolean,

        @Value("\${$WINDOW_INTERVALS_MIN:$WINDOW_INTERVALS_MIN_DEFAULT}")
        private val windowIntervalsString: String
) {

    @Bean
    fun windowHop(): Long = windowHopSec convert SECONDS_TO_MILLIS

    @Bean
    fun pollTimeout(): Long = (windowHop() * POLL_TIMEOUT_COEFFICIENT).toLong()

    @Bean
    fun windowIntervals(): MutableSet<Long> = windowIntervalsString
            .split(",")
            .map { it -> TimeUnit.MINUTES.toMillis(it.toLong()) }
            .toMutableSet()
            .apply {
                add(Intervals.MINUTE)
                add(Intervals.HOUR)
                add(Intervals.DAY)
            }

    @Bean
    fun allowNotClosedWindows() = allowNotClosedWindows
}