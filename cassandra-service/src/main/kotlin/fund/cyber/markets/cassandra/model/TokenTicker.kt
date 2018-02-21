package fund.cyber.markets.cassandra.model

import fund.cyber.markets.model.TokenTicker
import java.math.BigDecimal

/**
 * TokenTicker class for cassandra
 *
 * @property price - map of  BCT_Symbol(Base Tokens) -> Exchange -> TokenPrice
 * @property volume - map of CT_Symbol -> Exchange -> Volume
 * @property baseVolume - map of BCT_Symbol -> exchange -> TotalVolume in BCT
 */
data class CqlTokenTicker(
        val symbol: String,

        var timestampFrom: Long,
        var timestampTo: Long,
        val interval: Long,

        val volume: MutableMap<String, MutableMap<String, BigDecimal>>,
        val baseVolume: MutableMap<String, MutableMap<String, BigDecimal>>,
        val price: MutableMap<String, MutableMap<String, CqlTokenPrice>> = mutableMapOf()
) {
    constructor(tokenTicker: TokenTicker) : this(
            tokenTicker.symbol,
            tokenTicker.timestampFrom,
            tokenTicker.timestampTo,
            tokenTicker.interval, tokenTicker.volume,
            tokenTicker.baseVolume) {

        tokenTicker.price.forEach { baseTokenSymbol, exchangeMap ->
            exchangeMap.forEach { exchange, tokenPrice ->
                price
                        .getOrPut(baseTokenSymbol, { mutableMapOf() })
                        .getOrPut(exchange, { CqlTokenPrice(tokenPrice.value!!) })
            }
        }
    }
}

data class CqlTokenPrice(
        var value: BigDecimal
)