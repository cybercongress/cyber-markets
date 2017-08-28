package fund.cyber.markets.common

/**
 * Queue with limited size, which overrides elements like it a circle.
 *
 * @author Ibragimov Ruslan
 * @since 0.2.4
 */
class CircularQueueKotlin<T>(
    private val maxSize: Int
) {
    private val _elements = arrayOfNulls<Any>(maxSize)
    private var index = 0

    fun addNext(element: T) {
        _elements[index++] = element
        if (index == maxSize) index = 0
    }

    val elements: Array<T?>
        get() = _elements.clone() as Array<T?>
}