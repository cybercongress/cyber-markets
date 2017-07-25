package fund.cyber.markets.helpers

import kotlinx.coroutines.experimental.delay
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit


private object Helpers {
    val LOGGER = LoggerFactory.getLogger(Helpers::class.java)!!
}

suspend fun <T, R> T.retryUntilSuccess(retryDelay: Long = 5, block: suspend () -> R): R {
    while (true) {
        try {
            return block()
        } catch (e: Exception) {
            Helpers.LOGGER.debug("Error during trying execute block", e)
            delay(retryDelay, TimeUnit.SECONDS)
        }
    }
}
