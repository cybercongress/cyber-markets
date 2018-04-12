package fund.cyber.markets.common

const val MILLIS_TO_SECONDS: Double = 1.0/1000
const val MILLIS_TO_MINUTES:Double = 1.0/1000/60
const val MILLIS_TO_HOURS:Double = 1.0/1000/60/60

const val SECONDS_TO_MILLIS:Double = 1000.0
const val SECONDS_TO_MINUTES:Double = 1.0/60
const val SECONDS_TO_HOURS:Double = 1.0/60/60

const val MINUTES_TO_MILLIS:Double = 1000.0
const val MINUTES_TO_SECONDS:Double = 60.0
const val MINUTES_TO_HOURS:Double = 1.0/60

infix fun Long.convert(coefficient: Double): Long {
    return (this * coefficient).toLong()
}