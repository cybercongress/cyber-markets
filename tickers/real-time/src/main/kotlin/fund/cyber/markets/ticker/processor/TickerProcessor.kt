package fund.cyber.markets.ticker.processor

import fund.cyber.markets.common.closestSmallerMultiply
import fund.cyber.markets.common.closestSmallerMultiplyFromTs
import fund.cyber.markets.common.model.TokenTicker
import fund.cyber.markets.ticker.common.addHop
import fund.cyber.markets.ticker.common.minusHop
import fund.cyber.markets.ticker.common.updatePrices
import fund.cyber.markets.ticker.service.TickerService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * @param tickers - map of TokenSymbol -> WindowDuration -> TokenTicker
 * @param windows - map of TokenSymbol -> WindowDuration -> Queue<TokenTicker>
 */
@Component
class TickerProcessor(
    private val hopTickerProcessor: HopTickerProcessor,
    private val tickerService: TickerService,
    private val windowHop: Long,
    @Qualifier("windowIntervals")
    private val windowIntervals: Set<Long>
) {
    private val log = LoggerFactory.getLogger(TickerProcessor::class.java)!!

    private val tickers: MutableMap<String, MutableMap<Long, TokenTicker>> = mutableMapOf()
    private val windows: MutableMap<String, MutableMap<Long, Queue<TokenTicker>>> = mutableMapOf()

    fun process() {
        while (true) {
            sleep(windowHop)

            val hopTickers = hopTickerProcessor.getHopTickers()
            merge(hopTickers)
        }
    }

    fun merge(hopTickers: MutableMap<String, TokenTicker>) {
        hopTickers.forEach { tokenSymbol, hopTicker ->
            windowIntervals.forEach { duration ->

                val ticker = getTicker(tokenSymbol, duration)
                val window = getWindow(tokenSymbol, duration)

                window.offer(hopTicker)
                ticker addHop hopTicker
            }
        }

        cleanupOldData()
        saveAndProduceToKafka()
        updateTimestamps()
    }

    private fun cleanupOldData() {
        val tickersForDelete = mutableListOf<TokenTicker>()

        tickers.forEach { tokenSymbol, windowDurationMap ->
            windowDurationMap.forEach { windowDuration, ticker ->

                val window = windows[tokenSymbol]!![windowDuration]!!
                while (window.isNotEmpty() && window.peek().timestampTo <= ticker.timestampFrom) {
                    ticker minusHop window.poll()
                }
                if (window.isEmpty()) {
                    tickersForDelete.add(ticker)
                } else {
                    ticker updatePrices window
                }
            }
        }

        tickersForDelete.forEach { ticker ->
            tickers[ticker.symbol]!!.remove(ticker.interval)
        }
    }

    fun saveAndProduceToKafka() {
        tickerService.persist(tickers, hopTickerProcessor.currentHopTo)
    }

    fun updateTimestamps() {
        val currentHopFromMillis = hopTickerProcessor.currentHopTo

        tickers.forEach { _, windowDurationMap ->
            windowDurationMap.forEach { _, ticker ->
                if (ticker.timestampTo <= currentHopFromMillis) {
                    val difference = currentHopFromMillis - ticker.timestampTo + windowHop
                    ticker.timestampFrom += difference
                    ticker.timestampTo += difference
                }
            }
        }
    }

    private fun getTicker(tokenSymbol: String, windowDuration: Long): TokenTicker {
        val timestampFrom = closestSmallerMultiplyFromTs(windowDuration)

        return tickers
            .getOrPut(tokenSymbol) { mutableMapOf() }
            .getOrPut(windowDuration) {
                TokenTicker(
                    symbol = tokenSymbol,
                    timestampFrom = timestampFrom,
                    timestampTo = timestampFrom + windowDuration,
                    interval = windowDuration)
            }
    }

    private fun getWindow(tokenSymbol: String, windowDuration: Long): Queue<TokenTicker> {
        return windows
            .getOrPut(tokenSymbol) { mutableMapOf() }
            .getOrPut(windowDuration) { LinkedList<TokenTicker>() }
    }

    private fun sleep(windowHop: Long) {
        val currentMillisHop = closestSmallerMultiply(System.currentTimeMillis(), windowHop)
        val diff = currentMillisHop + windowHop - System.currentTimeMillis()
        log.debug("Time for hop calculation: {} ms", windowHop - diff)
        TimeUnit.MILLISECONDS.sleep(diff)
    }

}