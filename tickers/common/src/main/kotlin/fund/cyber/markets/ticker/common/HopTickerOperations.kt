package fund.cyber.markets.ticker.common

import fund.cyber.markets.common.model.BaseTokens
import fund.cyber.markets.common.model.TickerPrice
import fund.cyber.markets.common.model.TokenTicker
import java.math.BigDecimal
import java.util.*

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
            updatePricesMap(this, hopTicker)

            //update baseVolumes
            hopTicker.baseVolume[baseTokenSymbol]?.forEach { exchange, hopTotalVolume ->
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

    //update baseVolumes
    BaseTokens.values()
        .map { it.name }
        .forEach { baseTokenSymbol ->
            hopTicker.baseVolume[baseTokenSymbol]?.forEach { exchange, hopTotalVolume ->
                val tickerBaseVolume = baseVolume[baseTokenSymbol]!![exchange]!!
                baseVolume[baseTokenSymbol]!![exchange] = tickerBaseVolume.minus(hopTotalVolume)
            }
        }
}

infix fun TokenTicker.updatePrices(window: Queue<TokenTicker>) {
    this.price.clear()

    window.forEach { hopTicker ->
        updatePricesMap(this, hopTicker)
    }
}

private fun updatePricesMap(ticker: TokenTicker, hopTicker: TokenTicker) {
    hopTicker.price.forEach { baseTokenSymbol, exchangeMap ->
        exchangeMap.forEach { exchange, hopTickerPrice ->
            val tickerPrice = ticker.price
                .getOrPut(baseTokenSymbol) { mutableMapOf() }
                .getOrPut(exchange) { TickerPrice(hopTickerPrice.open) }

            ticker.price[baseTokenSymbol]!![exchange] = tickerPrice.update(hopTickerPrice)
        }
    }
}