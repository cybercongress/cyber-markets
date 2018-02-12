package fund.cyber.markets.common

import com.fasterxml.jackson.databind.PropertyNamingStrategy


/**
 * Simple strategy where external name simply only uses upper-case characters,
 * and no separators.
 * Conversion from internal name like "someOtherValue" would be into external name
 * if "SOMEOTHERVALUE".
 */
open class UpperCaseNamingStrategy : PropertyNamingStrategy.PropertyNamingStrategyBase() {

    /**
     * Converts words to upper-case
     *
     * For example, "userName" would be converted to
     * "USERNAME".
     *
     * @param input string
     * @return input converted to UpperCase format
     */
    override fun translate(input: String?): String? {
        if (input == null || input.length == 0) {
            return input // garbage in, garbage out
        }

        return input.toUpperCase()
    }

}