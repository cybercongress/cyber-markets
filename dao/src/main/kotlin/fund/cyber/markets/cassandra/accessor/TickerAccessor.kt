package fund.cyber.markets.cassandra.accessor

import com.datastax.driver.mapping.Result
import com.datastax.driver.mapping.annotations.Accessor
import com.datastax.driver.mapping.annotations.Param
import com.datastax.driver.mapping.annotations.Query
import fund.cyber.markets.dto.TokensPair
import fund.cyber.markets.model.Ticker
import java.util.*


@Accessor
interface TickerAccessor {
    @Query("SELECT * FROM ticker WHERE " +
            "pair=:pair " +
            "AND windowDuration=:windowDuration " +
            "AND exchange=:exchange " +
            "AND timestampTo>=:timestamp " +
            "LIMIT :limitValue")
    fun getTickers(
            @Param("pair") pair: TokensPair,
            @Param("windowDuration") windowDuration: Long,
            @Param("exchange") exchange: String,
            @Param("timestamp") timestamp: Date,
            @Param("limitValue") limit: Int
    ): Result<Ticker>
}