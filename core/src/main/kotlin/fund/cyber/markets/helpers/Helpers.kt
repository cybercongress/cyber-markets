package fund.cyber.markets.helpers

import kotlinx.coroutines.experimental.delay
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

private object Helpers {
    val LOGGER = LoggerFactory.getLogger(Helpers::class.java)!!
}

suspend fun <R> retryUntilSuccess(retryDelay: Long = 5, block: suspend () -> R): R {
    while (true) {
        try {
            return block()
        } catch (e: Exception) {
            Helpers.LOGGER.debug("Error during trying execute block", e)
            delay(retryDelay, TimeUnit.SECONDS)
        }
    }
}

inline fun <reified T : Any> env(name: String, default: T): T =
    when (T::class) {
        String::class -> (System.getenv(name) ?: default) as T
        Int::class, Int::class.javaPrimitiveType -> (System.getenv(name)?.toIntOrNull() ?: default) as T
        Boolean::class, Boolean::class.javaPrimitiveType -> (System.getenv(name).toBoolean()) as T
        else -> default
    }
