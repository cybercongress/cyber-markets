package fund.cyber.markets.cassandra.accessor

import com.datastax.driver.mapping.Result
import com.datastax.driver.mapping.annotations.Accessor
import com.datastax.driver.mapping.annotations.Param
import com.datastax.driver.mapping.annotations.Query
import fund.cyber.markets.cassandra.model.CqlOrderBook
import fund.cyber.markets.cassandra.model.CqlTokensPair
import java.util.*

@Accessor
interface OrderBookAccessor {

    @Query("SELECT * FROM markets.orderbook " +
        "WHERE exchange=:exchange AND pair=:pair AND epochHour=:epochHour AND timestamp<=:timestamp;")
    fun getNearest(@Param exchange: String, @Param pair: CqlTokensPair, @Param epochHour: Long, @Param timestamp: Date): Result<CqlOrderBook>
}