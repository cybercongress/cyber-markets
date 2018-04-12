package fund.cyber.markets.api.common

import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable

/**
 * Test for [CircularQueue]
 *
 * @author Ibragimov Ruslan
 * @since 0.2.4
 */
class CircularQueueTest {
    @Test
    fun `max size zero`() {
        assertThrows(ArrayIndexOutOfBoundsException::class.java, {
            CircularQueue<Int>(0).addNext(1)
        })
    }

    @Test
    fun `negative max size`() {
        assertThrows(NegativeArraySizeException::class.java, {
            CircularQueue<Int>(-1).addNext(1)
        })
    }

    @Test
    fun `max size of one`() {
        val queue = CircularQueue<Int>(1)

        queue.addNext(1)
        assertIterableEquals(listOf(1), queue.elements)

        queue.addNext(2)
        assertIterableEquals(listOf(2), queue.elements)

        queue.addNext(3)
        assertIterableEquals(listOf(3), queue.elements)
    }

    @Test
    fun `max size of two`() {
        val queue = CircularQueue<Int>(2)

        queue.addNext(1)
        assertIterableEquals(listOf(1), queue.elements)

        queue.addNext(2)
        assertIterableEquals(listOf(1, 2), queue.elements)

        queue.addNext(3)
        assertIterableEquals(listOf(2, 3), queue.elements)
    }

    @Test
    fun `max size of three`() {
        val queue = CircularQueue<Int>(3)

        queue.addNext(1)
        queue.addNext(2)
        queue.addNext(3)
        queue.addNext(4)
        assertIterableEquals(listOf(2, 3, 4), queue.elements)

        queue.addNext(5)
        assertIterableEquals(listOf(3, 4, 5), queue.elements)
    }

    @Test
    fun `get non existing`() {
        val queue = CircularQueue<Int>(3)

        assertAll(
            Executable { assertEquals(null, queue[0]) },
            Executable { assertEquals(null, queue[1]) },
            Executable { assertEquals(null, queue[2]) },
            Executable { assertEquals(null, queue[3]) }
        )
    }

    @Test
    fun `get existing`() {
        val queue = CircularQueue<Int>(3)

        queue.addNext(1)
        queue.addNext(2)
        queue.addNext(3)

        assertAll(
            Executable { assertEquals(1, queue[0]) },
            Executable { assertEquals(2, queue[1]) },
            Executable { assertEquals(3, queue[2]) },
            Executable { assertEquals(null, queue[3]) }
        )
    }
}