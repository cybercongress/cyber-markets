package fund.cyber.markets.helpers

import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.xnio.IoFuture
import org.xnio.IoFuture.Notifier
import org.xnio.IoFuture.Status.*
import java.io.IOException
import kotlin.coroutines.experimental.Continuation
import kotlin.coroutines.experimental.suspendCoroutine


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
            override fun onFailure(call: Call?, e: IOException) {cont.resumeWithException(e)}
            override fun onResponse(call: Call?, response: Response) {cont.resume(response)}
        }
        enqueue(callback)
    }
}
