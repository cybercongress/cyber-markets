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

    @Test
    @DisplayName("base and quote was compare by dictionary")
    fun testInitTokenPairWithCompareBaseAndQuoteByDictionary() {
        val base = "BTC"
        val quote = "USDT"
        val correctTokenPair = TokensPair(base,quote)
        val incorrectTokenPair = TokensPair(quote, base)

        //will be BTC_USDT pair
        Assertions.assertEquals(correctTokenPair.base, incorrectTokenPair.base)
        Assertions.assertEquals(correctTokenPair.quote, incorrectTokenPair.quote)
        Assertions.assertEquals(correctTokenPair.base, base)
        Assertions.assertEquals(correctTokenPair.quote, quote)
    }

    @Test
    @DisplayName("Token pair reverted if needed")
    fun testTokenPairReverted() {
        val base = "BTC"
        val quote = "USDT"
        val nonRevertedTokenPair = TokensPair(base,quote)
        val revertedTokenPair = TokensPair(quote, base)

        //will be BTC_USDT pair
        Assertions.assertEquals(nonRevertedTokenPair.base, revertedTokenPair.base)
        Assertions.assertEquals(nonRevertedTokenPair.quote, revertedTokenPair.quote)
        Assertions.assertEquals(nonRevertedTokenPair.base, base)
        Assertions.assertEquals(nonRevertedTokenPair.quote, quote)
        Assertions.assertTrue(revertedTokenPair.reverted)
        Assertions.assertFalse(nonRevertedTokenPair.reverted)
    }
    
}