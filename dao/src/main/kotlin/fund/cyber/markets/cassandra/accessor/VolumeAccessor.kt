package fund.cyber.markets.cassandra.accessor

import com.datastax.driver.mapping.Result
import com.datastax.driver.mapping.annotations.Accessor
import com.datastax.driver.mapping.annotations.Param
import com.datastax.driver.mapping.annotations.Query
import fund.cyber.markets.model.TokenVolume
import java.util.*


@Accessor
interface VolumeAccessor {
    @Query("SELECT * FROM volume WHERE " +
            "tokensymbol=:tokensymbol " +
            "AND windowDuration=:windowDuration " +
            "AND exchange=:exchange " +
            "AND timestampTo>:timestamp")
    fun getVolumes(
            @Param("tokensymbol") token: String,
            @Param("windowDuration") windowDuration: Long,
            @Param("exchange") exchange: String,
            @Param("timestamp") timestamp: Date
    ): Result<TokenVolume>
}