package fund.cyber.markets.connectors.helpers

import fund.cyber.markets.connectors.applicationContext
import fund.cyber.markets.connectors.applicationSingleThreadContext
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.CoroutineStart
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch
import kotlin.coroutines.experimental.CoroutineContext

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