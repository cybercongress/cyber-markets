package fund.cyber.markets.model

import com.datastax.driver.mapping.annotations.UDT

/**
 * @author mgergalov
 */
@UDT(name = "tokenpair")
data class TokensPair(
        val base: String,
        val quote: String
)