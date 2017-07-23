package fund.cyber.markets.configuration

val WS_CONNECTION_IDLE_TIMEOUT: Long = 10
val SCHEDULER_POOL_SIZE = 5

/**
 * @property wsPoolPeriod Period of checking connection in ms
 */
data class ApplicationConfig(
    val wsPoolPeriod: Long = 10000L
)

val config = ApplicationConfig()