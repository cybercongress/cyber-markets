package fund.cyber.markets.helpers

import kotlinx.coroutines.experimental.delay
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass

/**
 * Helper function to create logger.
 *
 * @author Ibragimov Ruslan
 */
fun logger(klass: KClass<*>): Logger = LoggerFactory.getLogger(klass.java)

private object Helpers {
    val LOGGER = logger(Helpers::class)
}

suspend fun <T, R> T.retryUntilSuccess(retryDelay: Long = 5, block: suspend () -> R): R {
    while (true) {
        try {
            return block()
        } catch (e: Exception) {
            Helpers.LOGGER.error("Retryable block completes exceptionally", e)
            delay(retryDelay, TimeUnit.SECONDS)
        }
    }
}

