package fund.cyber.markets.cassandra.accessor

import com.datastax.driver.mapping.Result
import com.datastax.driver.mapping.annotations.Accessor
import com.datastax.driver.mapping.annotations.Param
import com.datastax.driver.mapping.annotations.Query
import fund.cyber.markets.cassandra.model.CqlTokensPair
import fund.cyber.markets.cassandra.model.CqlTrade

@Accessor
interface TradeAccessor {

    @Query("SELECT * FROM markets.trade " +
        "WHERE exchange=:exchange AND pair=:pair AND epochMinute=:epochHour;")
    fun get(@Param exchange: String, @Param pair: CqlTokensPair, @Param epochMinute: Long): Result<CqlTrade>
}