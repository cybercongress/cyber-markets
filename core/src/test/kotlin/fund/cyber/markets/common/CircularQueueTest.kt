package fund.cyber.markets.common

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

/**
 * Test for [CircularQueueKotlin]
 *
 * @author Ibragimov Ruslan
 * @since 0.2.4
 */
class CircularQueueTest {
    @Test
    fun `max size zero`() {
        assertThrows(ArrayIndexOutOfBoundsException::class.java, {
            CircularQueueKotlin<Int>(0).addNext(1)
        })
    }

    @Test
    fun `negative max size`() {
        assertThrows(NegativeArraySizeException::class.java, {
            CircularQueueKotlin<Int>(-1).addNext(1)
        })
    }

    @Test
    fun `max size of one`() {
        val queue = CircularQueueKotlin<Int>(1)
        queue.addNext(1)
        assertArrayEquals(arrayOf(1), queue.elements)
        queue.addNext(2)
        assertArrayEquals(arrayOf(2), queue.elements)
        queue.addNext(3)
        assertArrayEquals(arrayOf(3), queue.elements)
    }

    @Test
    fun `max size of two`() {
        val queue = CircularQueueKotlin<Int>(2)
        queue.addNext(1)
        assertArrayEquals(arrayOf(1, null), queue.elements)
        queue.addNext(2)
        assertArrayEquals(arrayOf(1, 2), queue.elements)
        queue.addNext(3)
        assertArrayEquals(arrayOf(3, 2), queue.elements)
    }
}