package fund.cyber.markets.helpers

import kotlinx.coroutines.experimental.CancellableContinuation
import kotlinx.coroutines.experimental.suspendCancellableCoroutine
import org.knowm.xchange.ExchangeFactory
import org.springframework.util.concurrent.ListenableFuture
import java.util.concurrent.ExecutionException

/**
 * @author Ibragimov Ruslan
 */
inline fun <reified T : Any> ExchangeFactory.createExchange(): T {
    return ExchangeFactory.INSTANCE.createExchange(T::class.java.name) as T
}

suspend fun <T> ListenableFuture<T>.await(): T {
    if (isDone) {
        try {
            return get()
        } catch (e: ExecutionException) {
            throw e.cause ?: e // unwrap original cause from ExecutionException
        }
    }

    return suspendCancellableCoroutine { continuation: CancellableContinuation<T> ->
        this.addCallback({ result: T ->
            continuation.resume(result)
        }, { exception: Throwable ->
            continuation.resumeWithException(exception)
        })
    }
}
