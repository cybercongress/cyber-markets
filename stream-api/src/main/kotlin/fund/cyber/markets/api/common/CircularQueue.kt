package fund.cyber.markets.api.common

/**
 * Queue with limited size, which overrides elements like it a circle.
 *
 * @author Ibragimov Ruslan
 * @since 0.2.4
 */
class CircularQueue<T>(
    private val maxSize: Int
) {
    private val _elements = arrayOfNulls<Any>(maxSize) as Array<T>
    private var index = 0

    fun addNext(element: T) {
        _elements[index++] = element
        if (index == maxSize) index = 0
    }

    /**
     * Returns element by index or null
     */
    operator fun get(index: Int): T? =
        _elements.getOrNull(index)

    /**
     * Returns smaller list if queue not full,
     * either returns list of queue sorted from older to newer elements.
     */
    val elements: List<T>
        get() {
            return if (isFull) {
                if (index == 0) {
                    _elements.toList()
                } else {
                    _elements.slice(index until maxSize) +
                        _elements.slice(0 until index)
                }
            } else {
                _elements.slice(0 until index)
            }
        }

    private val isFull: Boolean
        get() = _elements[maxSize - 1] != null
}
