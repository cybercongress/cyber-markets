package fund.cyber.markets.helpers

import fund.cyber.markets.model.Ticker
import fund.cyber.markets.model.Trade
import java.math.BigDecimal
import java.util.*

infix fun Ticker.add(trade: Trade) {
    if (!validTrade(trade)) {
        return
    }

    if (open.compareTo(BigDecimal.ZERO) == 0) {
        open = trade.quoteAmount.div(trade.baseAmount)
        minPrice = open
        maxPrice = open
    }

    val tradePrice = trade.quoteAmount.div(trade.baseAmount)

    quoteAmount = quoteAmount.plus(trade.quoteAmount)
    baseAmount = baseAmount.plus(trade.baseAmount)

    minPrice = minPrice.min(tradePrice)
    maxPrice = maxPrice.max(tradePrice)

    close = tradePrice

    tradeCount++
}

infix fun Ticker.addHop(ticker: Ticker) {
    if (open.compareTo(BigDecimal.ZERO) == 0) {
        open = ticker.open
        minPrice = open
        maxPrice = open
    }

    quoteAmount = quoteAmount.plus(ticker.quoteAmount)
    baseAmount = baseAmount.plus(ticker.baseAmount)

    minPrice = minPrice.min(ticker.minPrice)
    maxPrice = maxPrice.max(ticker.maxPrice)

    close = ticker.close
    tradeCount += ticker.tradeCount
}

infix fun Ticker.minusHop(ticker: Ticker) {
    quoteAmount = quoteAmount.minus(ticker.quoteAmount)
    baseAmount = baseAmount.minus(ticker.baseAmount)

    tradeCount -= ticker.tradeCount
}

infix fun Ticker.findMinMaxPrice(window: Queue<Ticker>) {
    var min = window.peek().minPrice
    var max = window.peek().maxPrice

    for (hopTicker in window) {
        min = min.min(hopTicker.minPrice)
        max = max.max(hopTicker.maxPrice)
    }

    minPrice = min
    maxPrice = max
}

private fun validTrade(trade: Trade): Boolean {
    return !(trade.quoteAmount.compareTo(BigDecimal.ZERO) == 0 || trade.baseAmount.compareTo(BigDecimal.ZERO) == 0)
}