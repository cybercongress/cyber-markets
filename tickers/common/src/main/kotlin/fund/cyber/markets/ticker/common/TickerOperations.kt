package fund.cyber.markets.ticker.common

import fund.cyber.markets.common.model.Exchanges
import fund.cyber.markets.common.model.TickerPrice
import fund.cyber.markets.common.model.TokenTicker
import fund.cyber.markets.common.model.Trade
import java.math.BigDecimal

fun updateVolumes(tickers: MutableMap<String, TokenTicker>, trade: Trade, timestampFrom: Long, interval: Long) {
    val base = trade.pair.base
    val quote = trade.pair.quote
    val exchange = trade.exchange

    val tickerBase = getTicker(tickers, base, timestampFrom, interval)
    val tickerQuote = getTicker(tickers, quote, timestampFrom, interval)

    val volumeBase = tickerBase.volume
        .getOrPut(quote) { mutableMapOf() }
        .getOrPut(exchange) { BigDecimal.ZERO }
        .plus(trade.baseAmount)
    tickerBase.volume[quote]!![exchange] = volumeBase

    val volumeBaseAll = tickerBase.volume
        .getOrPut(quote) { mutableMapOf() }
        .getOrPut(Exchanges.ALL) { BigDecimal.ZERO }
        .plus(trade.baseAmount)
    tickerBase.volume[quote]!![Exchanges.ALL] = volumeBaseAll

    val volumeQuote = tickerQuote.volume
        .getOrPut(base) { mutableMapOf() }
        .getOrPut(exchange) { BigDecimal.ZERO }
        .plus(trade.quoteAmount)
    tickerQuote.volume[base]!![exchange] = volumeQuote

    val volumeQuoteAll = tickerQuote.volume
        .getOrPut(base) { mutableMapOf() }
        .getOrPut(Exchanges.ALL) { BigDecimal.ZERO }
        .plus(trade.quoteAmount)
    tickerQuote.volume[base]!![Exchanges.ALL] = volumeQuoteAll
}

fun updateBaseVolumesWithPrices(tickers: MutableMap<String, TokenTicker>, crossConversion: CrossConversion,
                                        baseTokenSymbol: String, trade: Trade, timestampFrom: Long, interval: Long) {
    val base = trade.pair.base
    val quote = trade.pair.quote
    val exchange = trade.exchange

    val tickerBase = getTicker(tickers, base, timestampFrom, interval)
    val tickerQuote = getTicker(tickers, quote, timestampFrom, interval)

    val priceForBase = crossConversion.calculate(base, baseTokenSymbol, exchange) ?: BigDecimal.ZERO
    val priceForQuote = crossConversion.calculate(quote, baseTokenSymbol, exchange) ?: BigDecimal.ZERO

    if (priceForBase > BigDecimal.ZERO) {
        val basePrice = tickerBase.price
            .getOrPut(baseTokenSymbol) { mutableMapOf() }
            .getOrPut(exchange) { TickerPrice(priceForBase) }
        tickerBase.price[baseTokenSymbol]!![exchange] = basePrice.update(priceForBase)

        val volumeBaseBCT = tickerBase.baseVolume
            .getOrPut(baseTokenSymbol) { mutableMapOf() }
            .getOrPut(exchange) { BigDecimal.ZERO }
            .plus(priceForBase.multiply(trade.baseAmount))
        tickerBase.baseVolume[baseTokenSymbol]!![exchange] = volumeBaseBCT

        val volumeBaseBCTall = tickerBase.baseVolume
            .getOrPut(baseTokenSymbol) { mutableMapOf() }
            .getOrPut(Exchanges.ALL) { BigDecimal.ZERO }
            .plus(priceForBase.multiply(trade.baseAmount))
        tickerBase.baseVolume[baseTokenSymbol]!![Exchanges.ALL] = volumeBaseBCTall
    }

    if (priceForQuote > BigDecimal.ZERO) {
        val quotePrice = tickerQuote.price
            .getOrPut(baseTokenSymbol) { mutableMapOf() }
            .getOrPut(exchange) { TickerPrice(priceForQuote) }
        tickerQuote.price[baseTokenSymbol]!![exchange] = quotePrice.update(priceForQuote)

        val volumeQuoteBCT = tickerQuote.baseVolume
            .getOrPut(baseTokenSymbol) { mutableMapOf() }
            .getOrPut(exchange) { BigDecimal.ZERO }
            .plus(priceForQuote.multiply(trade.quoteAmount))
        tickerQuote.baseVolume[baseTokenSymbol]!![exchange] = volumeQuoteBCT

        val volumeQuoteBCTall = tickerQuote.baseVolume
            .getOrPut(baseTokenSymbol) { mutableMapOf() }
            .getOrPut(Exchanges.ALL) { BigDecimal.ZERO }
            .plus(priceForQuote.multiply(trade.quoteAmount))
        tickerQuote.baseVolume[baseTokenSymbol]!![Exchanges.ALL] = volumeQuoteBCTall
    }
}

private fun getTicker(tickers: MutableMap<String, TokenTicker>, tokenSymbol: String, timestampFrom: Long, interval: Long): TokenTicker {
    return tickers
        .getOrPut(tokenSymbol) {
            TokenTicker(tokenSymbol, timestampFrom, timestampFrom + interval, interval)
        }
}