package fund.cyber.markets.exchanges.common

import fund.cyber.markets.model.TokensPair
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

/**
 * @author mgergalov
 */
@DisplayName("Every time when token pair created:")
class TokensPairTest {

    @Test
    @DisplayName("base and quote was compare alphabetically")
    fun testInitTokenPairWithCompareBaseAndQuote() {
        val firstInAlphabet = "aaa"
        val secondInAlphabet = "bbb"
        val comparedAttempt = TokensPair(firstInAlphabet,secondInAlphabet)
        val incomparableAttempt = TokensPair(secondInAlphabet, firstInAlphabet)

        Assertions.assertTrue(firstInAlphabet.compareTo(secondInAlphabet, true) < 0)
        Assertions.assertTrue(comparedAttempt.base.compareTo(comparedAttempt.quote, true) < 0)
        Assertions.assertEquals(comparedAttempt.base, incomparableAttempt.base)
        Assertions.assertEquals(comparedAttempt.quote, incomparableAttempt.quote)
    }
    
}