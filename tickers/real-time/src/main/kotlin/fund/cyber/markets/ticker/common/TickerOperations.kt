package fund.cyber.markets.ticker.common

import fund.cyber.markets.common.model.BaseTokens
import fund.cyber.markets.common.model.TickerPrice
import fund.cyber.markets.common.model.TokenTicker
import java.math.BigDecimal

infix fun TokenTicker.addHop(hopTicker: TokenTicker) {

    //update volumes
    hopTicker.volume.forEach { ctSymbol, exchangeVolumeMap ->
        exchangeVolumeMap.forEach { exchange, hopVolume ->
            val tickerVolume = volume
                .getOrPut(ctSymbol) { mutableMapOf() }
                .getOrPut(exchange) { BigDecimal.ZERO }

            volume[ctSymbol]!![exchange] = tickerVolume.plus(hopVolume)
        }
    }

    BaseTokens.values()
        .map { it.name }
        .forEach { baseTokenSymbol ->

            //update prices
            hopTicker.price[baseTokenSymbol]!!.forEach { exchange, hopTickerPrice ->
                val tickerPrice = price
                    .getOrPut(baseTokenSymbol) { mutableMapOf() }
                    .getOrPut(exchange) { TickerPrice(hopTickerPrice.close) }

                price[baseTokenSymbol]!![exchange] = tickerPrice.update(hopTickerPrice)
            }

            //update baseVolumes
            hopTicker.baseVolume[baseTokenSymbol]!!.forEach { exchange, hopTotalVolume ->
                val tickerBaseVolume = baseVolume
                    .getOrPut(baseTokenSymbol) { mutableMapOf() }
                    .getOrPut(exchange) { BigDecimal.ZERO }

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

    //todo: update prices

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