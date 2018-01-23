package fund.cyber.markets.util

fun closestSmallerMultiply(dividend: Long, divider: Long): Long {
    return dividend / divider * divider
}