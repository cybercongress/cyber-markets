package fund.cyber.markets.dto

import com.datastax.driver.mapping.annotations.UDT

/**
 * @author mgergalov
 */
@UDT(name = "tokenpair")
data class TokensPair(
        val base: String,
        val quote: String
)