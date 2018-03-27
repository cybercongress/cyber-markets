package fund.cyber.markets.ticker.configuration

import fund.cyber.markets.common.Durations
import fund.cyber.markets.configuration.env
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

private const val WINDOW_DURATIONS_MIN: String = "WINDOW_DURATIONS_MIN"
private const val WINDOW_HOP_SEC: String = "WINDOW_HOP_SEC"
private const val ALLOW_NOT_CLOSED_WINDOWS: String = "ALLOW_NOT_CLOSED_WINDOWS"
private const val POLL_TIMEOUT_COEFFICIENT: Double = 0.5

@Component
class TickersConfiguration(
        val windowHop: Long = TimeUnit.SECONDS.toMillis(env(WINDOW_HOP_SEC, 3)),
        val pollTimeout: Long = (windowHop * POLL_TIMEOUT_COEFFICIENT).toLong(),
        val allowNotClosedWindows: Boolean = env(ALLOW_NOT_CLOSED_WINDOWS, "true").toBoolean()
) {

    val windowDurations: MutableSet<Long> = env(WINDOW_DURATIONS_MIN, "1,5,15,30,60,180,240,360,720,1440")
            .split(",")
            .map { it -> TimeUnit.MINUTES.toMillis(it.toLong()) }
            .toMutableSet()
            .apply {
                add(Durations.MINUTE)
                add(Durations.HOUR)
                add(Durations.DAY)
            }
}