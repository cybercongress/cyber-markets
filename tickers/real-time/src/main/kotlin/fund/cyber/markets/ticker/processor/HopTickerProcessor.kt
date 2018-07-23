package fund.cyber.markets.ticker.processor

import fund.cyber.markets.common.closestSmallerMultiplyFromTs
import fund.cyber.markets.common.model.BaseTokens
import fund.cyber.markets.common.model.Exchanges
import fund.cyber.markets.common.model.TickerPrice
import fund.cyber.markets.common.model.TokenTicker
import fund.cyber.markets.common.model.Trade
import fund.cyber.markets.ticker.common.CrossConversion
import fund.cyber.markets.ticker.service.TickerService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.math.BigDecimal

/**
 * @param hopTickers - map of token symbol -> TokenTicker
 */
@Component
class HopTickerProcessor(
    private val tickerService: TickerService,
    private val crossConversion: CrossConversion,
    private val windowHop: Long
) {
    private val log = LoggerFactory.getLogger(HopTickerProcessor::class.java)!!

    val hopTickers: MutableMap<String, TokenTicker> = mutableMapOf()
    var currentHopToMillis: Long = 0L

    fun update() {
        hopTickers.clear()

        currentHopToMillis = closestSmallerMultiplyFromTs(windowHop)
        val currentHopFromMillis = currentHopToMillis - windowHop

        val tradeRecords = tickerService.poll()
        val tradesCount = tradeRecords.count()
        log.debug("Trades count: {}", tradesCount)

        //todo: magic filter
        val trades = tradeRecords
            .map { tradeRecord -> tradeRecord.value() }
            .filter { trade -> trade.timestamp - windowHop >= currentHopFromMillis }

        log.debug("Dropped trades count: {}", tradesCount - trades.size)

        updatePrices(trades)

        for (trade in trades) {

            val base = trade.pair.base
            val quote = trade.pair.quote
            val exchange = trade.exchange

            val tickerBase = hopTickers
                .getOrPut(base) {
                    TokenTicker(base, currentHopFromMillis, currentHopToMillis, windowHop)
                }
            val tickerQuote = hopTickers
                .getOrPut(quote) {
                    TokenTicker(quote, currentHopFromMillis, currentHopToMillis, windowHop)
                }

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

            BaseTokens.values().forEach { baseToken ->
                val baseTokenSymbol = baseToken.name

                val priceForBase = crossConversion.calculate(base, baseTokenSymbol, exchange) ?: BigDecimal.ZERO
                val priceForQuote = crossConversion.calculate(quote, baseTokenSymbol, exchange) ?: BigDecimal.ZERO

                if (priceForBase > BigDecimal.ZERO) {
                    val tickerBasePrice = tickerBase.price
                        .getOrPut(baseTokenSymbol) { mutableMapOf() }
                        .getOrPut(exchange) { TickerPrice(priceForBase) }
                    tickerBase.price[baseTokenSymbol]!![exchange] = tickerBasePrice.update(priceForBase)

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
                    val tickerQuotePrice = tickerQuote.price
                        .getOrPut(baseTokenSymbol) { mutableMapOf() }
                        .getOrPut(exchange) { TickerPrice(priceForQuote) }
                    tickerQuote.price[baseTokenSymbol]!![exchange] = tickerQuotePrice.update(priceForQuote)

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

        }

    }

    private fun updatePrices(trades: List<Trade>) {
        val prices = crossConversion.prices
        trades.forEach { trade ->
            prices
                .getOrPut(trade.pair.base) { mutableMapOf() }
                .getOrPut(trade.pair.quote) { mutableMapOf() }
                .put(trade.exchange, trade.price)
        }
    }

}