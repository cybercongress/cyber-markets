package fund.cyber.markets.ticker.processor

import fund.cyber.markets.helpers.closestSmallerMultiplyFromTs
import fund.cyber.markets.model.BaseTokens
import fund.cyber.markets.model.Exchanges
import fund.cyber.markets.model.TokenPrice
import fund.cyber.markets.model.TokenTicker
import fund.cyber.markets.model.Trade
import fund.cyber.markets.ticker.common.CrossConversion
import fund.cyber.markets.ticker.configuration.TickersConfiguration
import fund.cyber.markets.ticker.service.TickerService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.math.BigDecimal

/**
 * @param hopTickers - map of fromToken -> TokenTicker
 */
@Component
class HopTickerProcessorImpl(
        val hopTickers: MutableMap<String, TokenTicker> = mutableMapOf()
): HopTickerProcessor {

    private val log = LoggerFactory.getLogger(HopTickerProcessorImpl::class.java)!!

    @Autowired lateinit var configuration: TickersConfiguration
    @Autowired lateinit var tickerService: TickerService
    @Autowired lateinit var crossConversion: CrossConversion
    private val windowHop: Long by lazy { configuration.windowHop }
    var currentHopFromMillis: Long = 0L

    override fun update() {
        hopTickers.clear()

        currentHopFromMillis = closestSmallerMultiplyFromTs(windowHop)
        val currentMillisHopFrom = currentHopFromMillis - windowHop

        val tradeRecords = tickerService.poll()
        val tradesCount = tradeRecords.count()
        log.debug("Trades count: {}", tradesCount)

        //todo: magic filter
        val trades = tradeRecords
                .filter { it.timestamp() + windowHop >= currentMillisHopFrom }
                .map { it.value() }

        log.debug("Dropped trades count: {}", tradesCount - trades.size)

        updatePrices(trades)

        for (trade in trades) {

            val base = trade.pair.base
            val quote = trade.pair.quote
            val exchange = trade.exchange

            val tickerBase = hopTickers
                    .getOrPut(base, {
                        TokenTicker(base, currentMillisHopFrom, currentHopFromMillis, windowHop)
                    })
            val tickerQuote = hopTickers
                    .getOrPut(quote, {
                        TokenTicker(quote, currentMillisHopFrom, currentHopFromMillis, windowHop)
                    })

            val volumeBase = tickerBase.volume
                    .getOrPut(quote, { mutableMapOf() })
                    .getOrPut(exchange, { BigDecimal.ZERO })
                    .plus(trade.baseAmount)
            tickerBase.volume[quote]!![exchange] = volumeBase

            val volumeBaseAll = tickerBase.volume
                    .getOrPut(quote, { mutableMapOf() })
                    .getOrPut(Exchanges.ALL, { BigDecimal.ZERO })
                    .plus(trade.baseAmount)
            tickerBase.volume[quote]!![Exchanges.ALL] = volumeBaseAll

            val volumeQuote = tickerQuote.volume
                    .getOrPut(base, { mutableMapOf() })
                    .getOrPut(exchange, { BigDecimal.ZERO })
                    .plus(trade.quoteAmount)
            tickerQuote.volume[base]!![exchange] = volumeQuote

            val volumeQuoteAll = tickerQuote.volume
                    .getOrPut(base, { mutableMapOf() })
                    .getOrPut(Exchanges.ALL, { BigDecimal.ZERO })
                    .plus(trade.quoteAmount)
            tickerQuote.volume[base]!![Exchanges.ALL] = volumeQuoteAll


            BaseTokens.values().forEach { baseToken ->
                val baseTokenSymbol = baseToken.name

                val priceForBase = crossConversion.calculate(base, baseTokenSymbol, exchange) ?: BigDecimal.ZERO
                val priceForQuote = crossConversion.calculate(quote, baseTokenSymbol, exchange) ?: BigDecimal.ZERO

                tickerBase.price
                        .getOrPut(baseTokenSymbol, { mutableMapOf() })
                        .getOrPut(exchange, { TokenPrice(priceForBase) })

                tickerQuote.price
                        .getOrPut(baseTokenSymbol, { mutableMapOf() })
                        .getOrPut(exchange, { TokenPrice(priceForQuote) })

                val volumeBaseBCT = tickerBase.baseVolume
                        .getOrPut(baseTokenSymbol, { mutableMapOf() })
                        .getOrPut(exchange, { BigDecimal.ZERO })
                        .plus(priceForBase.multiply(trade.baseAmount))
                tickerBase.baseVolume[baseTokenSymbol]!![exchange] = volumeBaseBCT

                val volumeBaseBCTall = tickerBase.baseVolume
                        .getOrPut(baseTokenSymbol, { mutableMapOf() })
                        .getOrPut(Exchanges.ALL, { BigDecimal.ZERO })
                        .plus(priceForBase.multiply(trade.baseAmount))
                tickerBase.baseVolume[baseTokenSymbol]!![Exchanges.ALL] = volumeBaseBCTall

                val volumeQuoteBCT = tickerQuote.baseVolume
                        .getOrPut(baseTokenSymbol, { mutableMapOf() })
                        .getOrPut(exchange, { BigDecimal.ZERO })
                        .plus(priceForQuote.multiply(trade.quoteAmount))
                tickerQuote.baseVolume[baseTokenSymbol]!![exchange] = volumeQuoteBCT

                val volumeQuoteBCTall = tickerQuote.baseVolume
                        .getOrPut(baseTokenSymbol, { mutableMapOf() })
                        .getOrPut(Exchanges.ALL, { BigDecimal.ZERO })
                        .plus(priceForQuote.multiply(trade.quoteAmount))
                tickerQuote.baseVolume[baseTokenSymbol]!![Exchanges.ALL] = volumeQuoteBCTall
            }

        }

    }

    private fun updatePrices(trades: List<Trade>) {
        val prices = crossConversion.prices
        trades.forEach { trade ->
            prices
                    .getOrPut(trade.pair.base, { mutableMapOf() })
                    .getOrPut(trade.pair.quote, { mutableMapOf() })
                    .put(trade.exchange, trade.spotPrice)
        }
    }

}