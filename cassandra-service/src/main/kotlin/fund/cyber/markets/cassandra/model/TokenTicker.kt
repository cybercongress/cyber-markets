package fund.cyber.markets.cassandra.model

import com.datastax.driver.mapping.annotations.ClusteringColumn
import com.datastax.driver.mapping.annotations.Column
import com.datastax.driver.mapping.annotations.Frozen
import com.datastax.driver.mapping.annotations.PartitionKey
import com.datastax.driver.mapping.annotations.Table
import com.datastax.driver.mapping.annotations.UDT
import fund.cyber.markets.model.TokenTicker
import java.math.BigDecimal
import java.util.*

/**
 * TokenTicker class for cassandra
 *
 * @property price - map of  BCT_Symbol(Base Tokens) -> Exchange -> TokenPrice
 * @property volume - map of CT_Symbol -> Exchange -> Volume
 * @property baseVolume - map of BCT_Symbol -> exchange -> TotalVolume in BCT
 */

@Table(keyspace = "markets", name = "ticker",
        readConsistency = "QUORUM", writeConsistency = "QUORUM",
        caseSensitiveKeyspace = false, caseSensitiveTable = false)
data class CqlTokenTicker(
        @PartitionKey(0) @Column(name = "tokensymbol")
        val symbol: String,

        @ClusteringColumn(0)
        var timestampFrom: Date,
        var timestampTo: Date,

        @ClusteringColumn(1)
        val interval: Long,

        @Frozen
        val volume: Map<String, Map<String, BigDecimal>>,
        @Frozen
        val baseVolume: Map<String, Map<String, BigDecimal>>,
        @Frozen
        val price: Map<String, Map<String, CqlTokenPrice>>
) {
    constructor(tokenTicker: TokenTicker) : this(
            tokenTicker.symbol,
            Date(tokenTicker.timestampFrom),
            Date(tokenTicker.timestampTo),
            tokenTicker.interval, tokenTicker.volume,
            tokenTicker.baseVolume,
            prices(tokenTicker))
}

private fun prices(tokenTicker: TokenTicker) =
        tokenTicker.price.mapValues { (_, exchangeMap) ->
            exchangeMap.mapValues { (_, tokenPrice) -> CqlTokenPrice(tokenPrice.value!!) }
        }

@UDT(name = "tokenprice")
data class CqlTokenPrice(
        var value: BigDecimal
)