package fund.cyber.markets.common

fun rand(from: Int, to: Int) = (Math.random() * (to - from) + from).toInt()

fun closestSmallerMultiply(dividend: Long, divider: Long): Long {
    return dividend / divider * divider
}

fun closestSmallerMultiplyFromTs(divider: Long): Long {
    return System.currentTimeMillis() / divider * divider
}