package fund.cyber.markets.common

import java.util.*

fun Deque<String>.booleanValue(): Boolean? = first?.toBoolean()
fun Deque<String>.intValue(): Int? = first?.toIntOrNull()
fun Deque<String>.longValue(): Long? = first?.toLongOrNull()
fun Deque<String>.stringValue(): String? = first