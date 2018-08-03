package fund.cyber.markets.ticker.processor

import fund.cyber.markets.common.closestSmallerMultiply
import fund.cyber.markets.common.model.BaseTokens
import fund.cyber.markets.common.model.TokenPrice
import fund.cyber.markets.common.model.TokenTicker
import fund.cyber.markets.ticker.common.CrossConversion
import fund.cyber.markets.ticker.common.updateBaseVolumesWithPrices
import fund.cyber.markets.ticker.common.updateVolumes
import fund.cyber.markets.ticker.processor.price.PriceProcessor
import fund.cyber.markets.ticker.service.TickerService
import fund.cyber.markets.ticker.service.TokenPriceService
import fund.cyber.markets.ticker.service.TradeService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import java.util.*

@Component
class HistoricalTickerProcessor(
    private val tradeService: TradeService,
    private val tickerService: TickerService,
    private val tokenPriceService: TokenPriceService,
    @Qualifier("windowIntervals")
    private val windowIntervals: Set<Long>,
    private val crossConversion: CrossConversion,
    private val lagFromRealTime: Long,
    private val priceProcessors: List<PriceProcessor>
) {
    private val log = LoggerFactory.getLogger(HistoricalTickerProcessor::class.java)!!

    private val tickers = mutableMapOf<String, TokenTicker>()

    fun start() {
        log.info("Start historical ticker processor. Timestamp: ${Date()}")

        val startTimestamp = tradeService.getLastProcessedTimestamp().block()!!.value

        log.info("Last processed timestamp: ${Date(startTimestamp)}")

        var lastTimestamp = startTimestamp
        val currentTimestamp = System.currentTimeMillis()

        windowIntervals.forEach { interval ->

            crossConversion.invalidatePrices()
            var timestampFrom = closestSmallerMultiply(startTimestamp - interval, interval)
            var timestampTo = timestampFrom + interval

            while (timestampTo < currentTimestamp - lagFromRealTime) {

                tickers.clear()
                val trades = tradeService
                    .getTrades(timestampFrom, timestampTo)
                    .collectList()
                    .defaultIfEmpty(mutableListOf())
                    .block()!!
                    .sortedBy { trade -> trade.timestamp }

                if (trades.isNotEmpty()) {
                    log.debug("Start timestamp: $timestampFrom. Interval: $interval. Trades count: ${trades.size}")

                    crossConversion.updatePrices(trades)

                    for (trade in trades) {
                        updateVolumes(tickers, trade, timestampFrom, interval)

                        BaseTokens.values().forEach { baseToken ->
                            updateBaseVolumesWithPrices(tickers, crossConversion, baseToken.name, trade, timestampFrom, interval)
                        }
                    }

                    tickerService.save(tickers.values).collectList().block()
                    tokenPriceService.save(getTokenPrices(tickers.values.toList())).collectList().block()
                }

                if (timestampTo > lastTimestamp) {
                    lastTimestamp = timestampTo
                }
                timestampFrom = timestampTo
                timestampTo += interval
            }

        }

        tradeService.updateLastProcessedTimestamp(lastTimestamp).block()
        log.info("Successfully completed tickers calculation. Timestamp: ${Date()} ")
    }

    private fun getTokenPrices(tickers: List<TokenTicker>): MutableList<TokenPrice> {
        val prices = mutableListOf<TokenPrice>()

        priceProcessors.forEach { processor ->
            prices.addAll(processor.calculate(tickers))
        }

        return prices
    }

}