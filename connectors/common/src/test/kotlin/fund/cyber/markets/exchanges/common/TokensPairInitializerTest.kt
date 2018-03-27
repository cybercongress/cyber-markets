package fund.cyber.markets.exchanges.common

import fund.cyber.markets.model.TokensPairInitializer
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

/**
 * @author mgergalov
 */
@DisplayName("Every time when token pair created:")
class TokensPairInitializerTest {

    @Test
    @DisplayName("base and quote was compare alphabetically")
    fun testInitTokenPairWithCompareBaseAndQuote() {
        val firstInAlphabet = "aaa"
        val secondInAlphabet = "bbb"
        val comparedAttempt = TokensPairInitializer(firstInAlphabet,secondInAlphabet)
        val incomparableAttempt = TokensPairInitializer(secondInAlphabet, firstInAlphabet)

        Assertions.assertTrue(firstInAlphabet.compareTo(secondInAlphabet, true) < 0)
        Assertions.assertTrue(comparedAttempt.pair.base.compareTo(comparedAttempt.pair.quote, true) < 0)
        Assertions.assertEquals(comparedAttempt.pair.base, incomparableAttempt.pair.base)
        Assertions.assertEquals(comparedAttempt.pair.quote, incomparableAttempt.pair.quote)
    }

    @Test
    @DisplayName("base and quote was compare by dictionary")
    fun testInitTokenPairWithCompareBaseAndQuoteByDictionary() {
        val base = "BTC"
        val quote = "USDT"
        val correctTokenPair = TokensPairInitializer(base,quote)
        val incorrectTokenPair = TokensPairInitializer(quote, base)

        //will be BTC_USDT pair
        Assertions.assertEquals(correctTokenPair.pair.base, incorrectTokenPair.pair.base)
        Assertions.assertEquals(correctTokenPair.pair.quote, incorrectTokenPair.pair.quote)
        Assertions.assertEquals(correctTokenPair.pair.base, base)
        Assertions.assertEquals(correctTokenPair.pair.quote, quote)
    }

    @Test
    @DisplayName("Token pair reverted if needed")
    fun testTokenPairReverted() {
        val base = "BTC"
        val quote = "USDT"
        val nonRevertedTokenPair = TokensPairInitializer(base,quote)
        val revertedTokenPair = TokensPairInitializer(quote, base)

        //will be BTC_USDT pair
        Assertions.assertEquals(nonRevertedTokenPair.pair.base, revertedTokenPair.pair.base)
        Assertions.assertEquals(nonRevertedTokenPair.pair.quote, revertedTokenPair.pair.quote)
        Assertions.assertEquals(nonRevertedTokenPair.pair.base, base)
        Assertions.assertEquals(nonRevertedTokenPair.pair.quote, quote)
        Assertions.assertTrue(revertedTokenPair.reverted)
        Assertions.assertFalse(nonRevertedTokenPair.reverted)
    }
    
}