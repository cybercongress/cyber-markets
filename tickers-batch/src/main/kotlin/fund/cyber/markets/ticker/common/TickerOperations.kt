/*
package fund.cyber.markets.ticker.common

import fund.cyber.markets.common.model.BaseTokens
import fund.cyber.markets.common.model.Exchanges
import fund.cyber.markets.common.model.TickerPrice
import fund.cyber.markets.common.model.TokenTicker
import fund.cyber.markets.common.model.Trade
import java.math.BigDecimal

infix fun TokenTicker.addHop(hopTicker: TokenTicker) {

    //update volumes
    hopTicker.volume.forEach { ctSymbol, exchangeVolumeMap ->
        exchangeVolumeMap.forEach { exchange, hopVolume ->
            val tickerVolume = volume
                    .getOrPut(ctSymbol, { mutableMapOf() })
                    .getOrPut(exchange, { BigDecimal.ZERO })

            volume[ctSymbol]!![exchange] = tickerVolume.plus(hopVolume)
        }
    }

    BaseTokens.values()
            .map { it.name }
            .forEach { baseTokenSymbol ->
                //update prices
                hopTicker.price[baseTokenSymbol]!!.forEach { exchange, hopTickerPrice ->
                    val tickerPrice = price
                            .getOrPut(baseTokenSymbol, { mutableMapOf() })
                            .getOrPut(exchange, { TickerPrice(BigDecimal.ZERO) })

                    if (!(tickerPrice.value != null && hopTickerPrice.value == null)) {
                        price[baseTokenSymbol]!![exchange]!!.value = hopTickerPrice.value
                    }
                }

                //update baseVolumes
                hopTicker.baseVolume[baseTokenSymbol]!!.forEach { exchange, hopTotalVolume ->
                    val tickerBaseVolume = baseVolume
                            .getOrPut(baseTokenSymbol, { mutableMapOf() })
                            .getOrPut(exchange, { BigDecimal.ZERO })

                    baseVolume[baseTokenSymbol]!![exchange] = tickerBaseVolume.plus(hopTotalVolume)
                }
            }
}

infix fun TokenTicker.minusHop(hopTicker: TokenTicker) {

    //update volumes
    hopTicker.volume.forEach { ctSymbol, exchangeVolumeMap ->
        exchangeVolumeMap.forEach { exchange, hopVolume ->
            val tickerVolume = volume[ctSymbol]!![exchange]!!
            volume[ctSymbol]!![exchange] = tickerVolume.minus(hopVolume)
        }
    }

    //update baseVolumes
    BaseTokens.values()
            .map { it.name }
            .forEach { baseTokenSymbol ->
                hopTicker.baseVolume[baseTokenSymbol]!!.forEach { exchange, hopTotalVolume ->
                    val tickerBaseVolume = baseVolume[baseTokenSymbol]!![exchange]!!
                    baseVolume[baseTokenSymbol]!![exchange] = tickerBaseVolume.minus(hopTotalVolume)
                }
            }
}

infix fun TokenTicker.merge(trade: Trade) {

    val baseSymbol = trade.pair.base
    val quoteSymbol = trade.pair.quote
    val exchange = trade.exchange

    // update price
    val baseTokensSymbols = BaseTokens.values().map { baseToken -> baseToken.name }
    //todo: check comparing timestamps
    if (baseTokensSymbols.contains(quoteSymbol) && trade.timestamp == timestampTo ) {
        price[quoteSymbol]!![exchange]!!.value = trade.price
    }

    // update volume
    if (symbol == baseSymbol) {

        val volumeBaseSymbol = volume
            .getOrPut(quoteSymbol, { mutableMapOf() })
            .getOrPut(exchange, { BigDecimal.ZERO })
            .plus(trade.baseAmount)
        volume[quoteSymbol]!![exchange] = volumeBaseSymbol

        val volumeBaseSymbolAll = volume
            .getOrPut(quoteSymbol, { mutableMapOf() })
            .getOrPut(Exchanges.ALL, { BigDecimal.ZERO })
            .plus(trade.baseAmount)
        volume[quoteSymbol]!![Exchanges.ALL] = volumeBaseSymbolAll
    } else {

        val volumeQuoteSymbol = volume
            .getOrPut(baseSymbol, { mutableMapOf() })
            .getOrPut(exchange, { BigDecimal.ZERO })
            .plus(trade.quoteAmount)
        volume[baseSymbol]!![exchange] = volumeQuoteSymbol

        val volumeQuoteSymbolAll = volume
            .getOrPut(baseSymbol, { mutableMapOf() })
            .getOrPut(Exchanges.ALL, { BigDecimal.ZERO })
            .plus(trade.quoteAmount)
        volume[baseSymbol]!![Exchanges.ALL] = volumeQuoteSymbolAll
    }


    // update volume in base symbols (BTC, ETH, USD)
    val crossConversion = CrossConversion(this)

    BaseTokens.values().forEach { baseToken ->
        val baseTokenSymbol = baseToken.name

        val priceForBase = crossConversion.calculate(baseSymbol, baseTokenSymbol, exchange) ?: BigDecimal.ZERO
        val priceForQuote = crossConversion.calculate(quoteSymbol, baseTokenSymbol, exchange) ?: BigDecimal.ZERO

        if (symbol == baseSymbol) {

            price
                .getOrPut(baseTokenSymbol, { mutableMapOf() })
                .getOrPut(exchange, { TickerPrice(priceForBase) })

            val volumeBaseBCT = baseVolume
                .getOrPut(baseTokenSymbol, { mutableMapOf() })
                .getOrPut(exchange, { BigDecimal.ZERO })
                .plus(priceForBase.multiply(trade.baseAmount))
            baseVolume[baseTokenSymbol]!![exchange] = volumeBaseBCT

            val volumeBaseBCTall = baseVolume
                .getOrPut(baseTokenSymbol, { mutableMapOf() })
                .getOrPut(Exchanges.ALL, { BigDecimal.ZERO })
                .plus(priceForBase.multiply(trade.baseAmount))
            baseVolume[baseTokenSymbol]!![Exchanges.ALL] = volumeBaseBCTall

        } else {

            price
                .getOrPut(baseTokenSymbol, { mutableMapOf() })
                .getOrPut(exchange, { TickerPrice(priceForQuote) })

            val volumeQuoteBCT = baseVolume
                .getOrPut(baseTokenSymbol, { mutableMapOf() })
                .getOrPut(exchange, { BigDecimal.ZERO })
                .plus(priceForQuote.multiply(trade.quoteAmount))
            baseVolume[baseTokenSymbol]!![exchange] = volumeQuoteBCT

            val volumeQuoteBCTall = baseVolume
                .getOrPut(baseTokenSymbol, { mutableMapOf() })
                .getOrPut(Exchanges.ALL, { BigDecimal.ZERO })
                .plus(priceForQuote.multiply(trade.quoteAmount))
            baseVolume[baseTokenSymbol]!![Exchanges.ALL] = volumeQuoteBCTall

        }
    }

}*/
