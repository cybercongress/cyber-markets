package fund.cyber.markets.tickers.processor

import fund.cyber.markets.common.Durations
import fund.cyber.markets.common.closestSmallerMultiply
import fund.cyber.markets.dto.TokensPair
import fund.cyber.markets.helpers.addHop
import fund.cyber.markets.helpers.findMinMaxPrice
import fund.cyber.markets.helpers.minusHop
import fund.cyber.markets.model.Ticker
import fund.cyber.markets.tickers.AppContext
import fund.cyber.markets.tickers.configuration.TickersConfiguration
import fund.cyber.markets.tickers.service.TickerService
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.sql.Timestamp
import java.util.*

class TickerProcessor(
        private val configuration: TickersConfiguration = AppContext.configuration,
        private val windowHop: Long = configuration.windowHop,
        private val windowDurations: Set<Long> = configuration.windowDurations,
        private val tickerService: TickerService = AppContext.tickerService,
        val tickers: MutableMap<TokensPair, MutableMap<String, MutableMap<Long, Ticker>>> = mutableMapOf(),
        val windows: MutableMap<TokensPair, MutableMap<String, MutableMap<Long, Queue<Ticker>>>> = mutableMapOf()
): TickerProcessorInterface {

    private val log = LoggerFactory.getLogger(TickerProcessor::class.java)!!

    override fun update(hopTickersProcessor: HopTickerProcessor, currentMillis: Long) {
        hopTickersProcessor.hopTickers.forEach { pair, exchangeMap ->
            exchangeMap.forEach { exchange, hopTicker ->

                for (windowDuration in windowDurations) {
                    val ticker = get(pair, exchange, windowDuration, currentMillis)
                    val window = getWindow(pair, exchange, windowDuration)

                    window.offer(hopTicker)
                    ticker addHop hopTicker
                }
            }
        }
        cleanupOldData()
    }

    override fun get(pair: TokensPair, exchange: String, windowDuration: Long, currentMillis: Long): Ticker {
        return tickers
                .getOrPut(pair, { mutableMapOf() })
                .getOrPut(exchange, { mutableMapOf() })
                .getOrPut(windowDuration, {
                    Ticker(pair, windowDuration, exchange,
                            Date(closestSmallerMultiply(currentMillis, windowDuration) + windowDuration),
                            Date(closestSmallerMultiply(currentMillis, windowDuration))
                    )
                })
    }

    override fun getWindow(pair: TokensPair, exchange: String, windowDuration: Long): Queue<Ticker> {
        return windows
                .getOrPut(pair, { mutableMapOf() })
                .getOrPut(exchange, { mutableMapOf() })
                .getOrPut(windowDuration, { LinkedList() })
    }

    override fun updateTimestamps(currentMillisHop: Long) {
        tickers.forEach { _, exchangeMap ->
            exchangeMap.forEach { _, windowDurMap ->
                windowDurMap.forEach { _, ticker ->
                    if (ticker.timestampTo!!.time <= currentMillisHop) {
                        val diff = currentMillisHop - ticker.timestampTo!!.time + windowHop
                        ticker.timestampFrom = Date(ticker.timestampFrom!!.time + diff)
                        ticker.timestampTo = Date(ticker.timestampTo!!.time + diff)
                    }
                }
            }
        }
    }

    private fun cleanupOldData() {
        tickers.forEach { pair, exchangeMap ->
            exchangeMap.forEach { exchange, windowDurationMap ->
                windowDurationMap.forEach { windowDuration, ticker ->

                    val window = windows[pair]!![exchange]!![windowDuration]!!
                    val windowCopy = LinkedList<Ticker>()
                    window.forEach {
                        windowCopy.add(it.copy())
                    }

                    while (windowCopy.peek() != null &&
                            windowCopy.peek().timestampTo!!.time <= ticker.timestampFrom!!.time) {

                        ticker minusHop windowCopy.poll()
                    }

                    if (!windowCopy.isEmpty()) {
                        ticker.open = windowCopy.peek().open
                        ticker findMinMaxPrice windowCopy
                    }

                }
            }
        }
    }

    override fun cleanupWindows() {

        tickers.forEach { pair, exchangeMap ->
            exchangeMap.forEach { exchange, windowDurMap ->

                val iterator = windowDurMap.iterator()
                while (iterator.hasNext()) {
                    val mapEntry = iterator.next()

                    val windowDuration = mapEntry.key
                    val ticker = mapEntry.value

                    val window = windows[pair]!![exchange]!![windowDuration]!!

                    while (window.peek() != null && window.peek().timestampTo!!.time <= ticker.timestampFrom!!.time) {
                        window.poll()
                    }

                    if (window.isEmpty()) {
                        iterator.remove()
                    }

                }
            }
        }
    }

    override fun calculatePrice(volumeProcessor: VolumeProcessor, currentMillisHop: Long) {
        val volumes = volumeProcessor.volumes

        windowDurations.forEach { windowDuration ->
            tickers.forEach { pair, exchangeMap ->

                var volume24h = BigDecimal.ZERO
                exchangeMap.forEach { exchange, windowDurMap ->
                    if (exchange != "ALL") {
                        val volume = volumes[pair.quote]!![exchange]!![Durations.DAY]
                        if (volume != null) {
                            volume24h = volume24h.add(volume.value)
                        }
                    }
                }

                val weightMap = mutableMapOf<String, BigDecimal>()
                exchangeMap.forEach { exchange, windowDurMap ->
                    if (exchange != "ALL") {
                        val volume24hByExchange = volumes.get(pair.quote)?.get(exchange)?.get(Durations.DAY)?.value
                        if (volume24hByExchange != null) {
                            weightMap[exchange] = volume24hByExchange.div(volume24h)
                        }
                    }
                }

                var avgPrice = BigDecimal(0)
                weightMap.forEach { exchange, weight ->
                    val ticker = exchangeMap[exchange]?.get(windowDuration)
                    if (ticker != null) {
                        val weightedPrice = ticker.close.multiply(weight)
                        avgPrice = avgPrice.plus(weightedPrice)
                    }
                }

                val tickerAllExchange = exchangeMap["ALL"]?.get(windowDuration)
                if (tickerAllExchange != null) {
                    tickerAllExchange.avgPrice = avgPrice
                }
            }
        }

        if (log.isTraceEnabled) {
            logTickers(currentMillisHop)
        }
    }

    override fun saveAndProduceToKafka(currentMillisHop: Long) {
        tickerService.saveAndProduceToKafka(tickers, currentMillisHop)
    }

    private fun logTickers(currentMillisHop: Long) {
        log.trace("Window timestamp: {}", Timestamp(currentMillisHop))
        tickers.forEach { _, exchangeMap ->
            exchangeMap.forEach { _, windowDurMap ->
                windowDurMap.forEach { _, ticker ->
                    if (ticker.timestampTo!!.time <= currentMillisHop) {
                        log.trace(ticker.toString())
                    }
                }
            }
        }
    }

}