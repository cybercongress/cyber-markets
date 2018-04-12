package fund.cyber.markets.ticker.configuration

import fund.cyber.markets.common.ALLOW_NOT_CLOSED_WINDOWS
import fund.cyber.markets.common.ALLOW_NOT_CLOSED_WINDOWS_DEFAULT
import fund.cyber.markets.common.Durations
import fund.cyber.markets.common.SECONDS_TO_MILLIS
import fund.cyber.markets.common.WINDOW_DURATIONS_MIN
import fund.cyber.markets.common.WINDOW_DURATIONS_MIN_DEFAULT
import fund.cyber.markets.common.WINDOW_HOP_SEC
import fund.cyber.markets.common.WINDOW_HOP_SEC_DEFAULT
import fund.cyber.markets.common.convert
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

private const val POLL_TIMEOUT_COEFFICIENT: Double = 0.5

@Component
class TickersConfiguration(
        @Value("\${$WINDOW_HOP_SEC:$WINDOW_HOP_SEC_DEFAULT}")
        private val windowHopSec: Long,

        @Value("\${$ALLOW_NOT_CLOSED_WINDOWS:$ALLOW_NOT_CLOSED_WINDOWS_DEFAULT}")
        val allowNotClosedWindows: Boolean,

        @Value("\${$WINDOW_DURATIONS_MIN:$WINDOW_DURATIONS_MIN_DEFAULT}")
        private val windowDurationsString: String
) {

    val windowHop: Long by lazy { windowHopSec convert SECONDS_TO_MILLIS }

    val pollTimeout: Long by lazy { (windowHop * POLL_TIMEOUT_COEFFICIENT).toLong() }

    val windowDurations: MutableSet<Long> = windowDurationsString
            .split(",")
            .map { it -> TimeUnit.MINUTES.toMillis(it.toLong()) }
            .toMutableSet()
            .apply {
                add(Durations.MINUTE)
                add(Durations.HOUR)
                add(Durations.DAY)
            }
}