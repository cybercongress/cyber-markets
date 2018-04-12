package fund.cyber.markets.helpers

import fund.cyber.markets.common.rand
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * @author Ibragimov Ruslan
 * @since 0.2.4
 */
class HelpersTest {
    @Test
    fun `zero rand`() {
        assertEquals(0, rand(0, 0))
    }
}