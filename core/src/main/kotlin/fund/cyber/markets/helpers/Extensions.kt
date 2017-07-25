package fund.cyber.markets.helpers

import org.xnio.IoFuture
import org.xnio.IoFuture.Notifier
import org.xnio.IoFuture.Status.*
import kotlin.coroutines.experimental.Continuation
import kotlin.coroutines.experimental.suspendCoroutine


suspend fun <T> IoFuture<T>.cAwait(): T {

    return suspendCoroutine { cont: Continuation<T> ->
        val notifier = Notifier<T, Any?> { ioFuture, _ ->
            when (ioFuture.status) {
                DONE -> cont.resume(get())
                FAILED -> cont.resumeWithException(exception)
                CANCELLED -> cont.resumeWithException(IllegalStateException("Canceled operation"))
                WAITING -> cont.resumeWithException(IllegalStateException("Awake during waiting operation"))
                null -> cont.resumeWithException(IllegalStateException("Unknown future state"))
            }

        }
        addNotifier(notifier, null)
    }
}