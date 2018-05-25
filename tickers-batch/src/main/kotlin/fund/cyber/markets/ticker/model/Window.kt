package fund.cyber.markets.ticker.model

import fund.cyber.markets.common.model.TokenTicker
import fund.cyber.markets.common.model.Trade
import java.math.BigDecimal

class Window(
    val timestampFrom: Long,
    val interval: Long,
    val trades: List<Trade> = mutableListOf<Trade>(),
    val prices: MutableMap<String, MutableMap<String, MutableMap<String, BigDecimal>>> = mutableMapOf(),
    val volumes: MutableMap<String, MutableMap<String, MutableMap<String, BigDecimal>>> = mutableMapOf(),
    val baseVolumes: MutableMap<String, MutableMap<String, MutableMap<String, BigDecimal>>> = mutableMapOf()
) {

    constructor(ticker: TokenTicker) : this(
        timestampFrom = ticker.timestampFrom,
        interval = ticker.interval
    ) {

        //init prices
        ticker.price.forEach { tokenSymbol, exchangeMap ->
            exchangeMap.forEach { exchange, tokenPrice ->

                prices
                    .getOrPut(exchange, { mutableMapOf() })
                    .getOrPut(ticker.symbol, { mutableMapOf() } )
                    .getOrPut(tokenSymbol, { tokenPrice.value!! } )

            }
        }

        //init volumes
        ticker.volume.forEach { tokenSymbol, exchangeMap ->
            exchangeMap.forEach { exchange, volume ->

                volumes
                    .getOrPut(exchange, { mutableMapOf() })
                    .getOrPut(ticker.symbol, { mutableMapOf() } )
                    .getOrPut(tokenSymbol, { volume } )

            }
        }

        //init volumes for base tokens
        ticker.baseVolume.forEach { tokenSymbol, exchangeMap ->
            exchangeMap.forEach { exchange, volume ->

                baseVolumes
                    .getOrPut(exchange, { mutableMapOf() })
                    .getOrPut(ticker.symbol, { mutableMapOf() } )
                    .getOrPut(tokenSymbol, { volume } )

            }
        }

    }

    fun put(trade: Trade) {
        trades
    }

}