package fund.cyber.markets.helpers

import fund.cyber.markets.applicationContext
import fund.cyber.markets.applicationSingleThreadContext
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.CoroutineStart
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit
import kotlin.coroutines.experimental.CoroutineContext


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

fun concurrent(
    context: CoroutineContext = applicationContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit
): Job = launch(context, start, block)

fun local(
    context: CoroutineContext = applicationSingleThreadContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit
): Job = launch(context, start, block)