package fund.cyber.markets.helpers

import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.xnio.IoFuture
import org.xnio.IoFuture.Notifier
import org.xnio.IoFuture.Status.CANCELLED
import org.xnio.IoFuture.Status.DONE
import org.xnio.IoFuture.Status.FAILED
import org.xnio.IoFuture.Status.WAITING
import java.io.IOException
import java.util.*
import kotlin.coroutines.experimental.Continuation
import kotlin.coroutines.experimental.suspendCoroutine

fun Deque<String>.booleanValue(): Boolean? = first?.toBoolean()
fun Deque<String>.intValue(): Int? = first?.toIntOrNull()
fun Deque<String>.longValue(): Long? = first?.toLongOrNull()
fun Deque<String>.stringValue(): String? = first

suspend fun <T> IoFuture<T>.cAwait(): T {

    return suspendCoroutine { cont: Continuation<T> ->
        val notifier = Notifier<T, Any?> { future, _ ->
            when (future.status) {
                DONE -> cont.resume(future.get())
                FAILED -> cont.resumeWithException(future.exception)
                CANCELLED -> cont.resumeWithException(IllegalStateException("Canceled operation"))
                WAITING -> cont.resumeWithException(IllegalStateException("Awake during waiting operation"))
                null -> cont.resumeWithException(IllegalStateException("Unknown future state"))
            }

        }
        addNotifier(notifier, null)
    }
}

suspend fun Call.await(): Response {

    return suspendCoroutine { cont: Continuation<Response> ->
        val callback = object : Callback {
            override fun onFailure(call: Call, e: IOException) {cont.resumeWithException(e)}
            override fun onResponse(call: Call, response: Response) {cont.resume(response)}
        }
        enqueue(callback)
    }
}
