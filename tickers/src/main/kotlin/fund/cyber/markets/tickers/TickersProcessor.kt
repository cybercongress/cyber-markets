package fund.cyber.markets.tickers

import fund.cyber.markets.common.Durations
import fund.cyber.markets.dto.TokensPair
import fund.cyber.markets.helpers.add
import fund.cyber.markets.helpers.addHop
import fund.cyber.markets.helpers.minusHop
import fund.cyber.markets.model.Ticker
import fund.cyber.markets.model.TokenVolume
import fund.cyber.markets.model.Trade
import fund.cyber.markets.tickers.configuration.TickersConfiguration
import fund.cyber.markets.tickers.service.TickerService
import fund.cyber.markets.tickers.service.VolumeService
import fund.cyber.markets.util.closestSmallerMultiply
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.sql.Timestamp
import java.util.*
import java.util.concurrent.TimeUnit

class TickersProcessor(
        private val configuration: TickersConfiguration,
        private val tickerService: TickerService,
        private val volumeService: VolumeService,
        private val windowHop: Long = configuration.windowHop,
        private val windowDurations: Set<Long> = configuration.windowDurations
) {

    private val log = LoggerFactory.getLogger(TickersProcessor::class.java)!!

    /**
     * The method that calculates tickers.
     *
     * Calculations are made in several steps:
     * - aggregation of trades from kafka topics to tickers with window duration equal to length of window hop.
     *   See {@link #calculateHopTickers(ConsumerRecords<String, Trade>,
     *                                   MutableMap<TokensPair,
     *                                   MutableMap<String, Ticker>>,
     *                                   Long) calculateHopTickers} method.
     *
     * - adding tickers to the queues that correspond to the windows with different pairs/exchanges/durations,
     *   aggregate a hopTickers from queues to final Ticker objects
     *   See {@link #updateTickers(MutableMap<TokensPair, MutableMap<String, Ticker>>,
     *                             MutableMap<TokensPair, MutableMap<String, MutableMap<Long, Ticker>>>,
     *                             MutableMap<TokensPair, MutableMap<String, MutableMap<Long, Queue<Ticker>>>>,
     *                             Long) updateTickers} method
     *
     * - cleaning the tickers from old hopTickers (hopTickers whose timestamp does not fall into the window)
     *   See {@link #cleanup(MutableMap<TokensPair, MutableMap<String, MutableMap<Long, Ticker>>>,
     *                              MutableMap<TokensPair, MutableMap<String, MutableMap<Long, Queue<Ticker>>>>)
     *   cleanup} method
     *
     * - calculation a price for each ticker and calculation a weighted average price for exchange called "ALL"
     *   See {@link #calculatePrice(MutableMap<TokensPair, MutableMap<String, MutableMap<Long, Ticker>>>)
     *   calculatePrice} method
     *
     * - produce updated tickers to kafka topic and save snapshots to db
     *   See {@link #saveAndProduceToKafka(MutableMap<TokensPair, MutableMap<String, MutableMap<Long, Ticker>>>,
     *                                     String,
     *                                     Long) saveAndProduceToKafka} method
     *
     * - update timestamps of tickers to next window hop time
     *   See {@link updateTimestamps(MutableMap<TokensPair, MutableMap<String, MutableMap<Long, Ticker>>>, Long)
     *   updateTimestamps} method
     */

    fun process() {

        val hopTickers = mutableMapOf<TokensPair, MutableMap<String, Ticker>>()
        val tickers = mutableMapOf<TokensPair, MutableMap<String, MutableMap<Long, Ticker>>>()
        val volumes = mutableMapOf<String, MutableMap<String, MutableMap<Long, TokenVolume>>>()
        val windows = mutableMapOf<TokensPair, MutableMap<String, MutableMap<Long, Queue<Ticker>>>>()

        sleep(windowHop)
        while (true) {

            val records = tickerService.poll(windowHop / 2)
            val currentMillis = System.currentTimeMillis()
            val currentMillisHop = closestSmallerMultiply(currentMillis, windowHop)
            log.debug("Trades count: {}", records.count())

            calculateHopTickers(records, hopTickers, currentMillisHop)

            updateTickers(hopTickers, tickers, windows, currentMillis)
            updateVolumes(hopTickers, volumes, currentMillis)

            cleanup(tickers, volumes, windows)

            calculatePrice(tickers, volumes)

            if (log.isTraceEnabled) {
                log(tickers, currentMillisHop)
            }

            tickerService.saveAndProduceToKafka(tickers, currentMillisHop)
            volumeService.saveAndProduceToKafka(volumes, currentMillisHop)

            updateTimestamps(tickers, volumes, currentMillisHop)

            hopTickers.clear()

            sleep(windowHop)
        }

    }

    private fun calculateHopTickers(records: ConsumerRecords<String, Trade>,
                                    hopTickers: MutableMap<TokensPair, MutableMap<String, Ticker>>,
                                    currentMillisHop: Long) {

        var droppedCount = 0
        for (record in records) {
            val currentMillisHopFrom = currentMillisHop - windowHop
            if (record.timestamp() < currentMillisHopFrom - windowHop) {
                droppedCount++
                continue
            }

            val trade = record.value()
            val ticker = hopTickers
                    .getOrPut(trade.pair, { mutableMapOf() })
                    .getOrPut(trade.exchange, {
                        Ticker(trade.pair, windowHop, trade.exchange, Date(currentMillisHop), Date(currentMillisHopFrom))
                    })
            val tickerAllExchange = hopTickers
                    .getOrPut(trade.pair, { mutableMapOf() })
                    .getOrPut("ALL", {
                        Ticker(trade.pair, windowHop, "ALL", Date(currentMillisHop), Date(currentMillisHopFrom))
                    })
            ticker add trade
            tickerAllExchange add trade
        }

        log.debug("Dropped trades count: {}", droppedCount)
    }

    private fun updateTickers(hopTickers: MutableMap<TokensPair, MutableMap<String, Ticker>>,
                              tickers: MutableMap<TokensPair, MutableMap<String, MutableMap<Long, Ticker>>>,
                              windows: MutableMap<TokensPair, MutableMap<String, MutableMap<Long, Queue<Ticker>>>>,
                              currentMillis: Long) {

        hopTickers.forEach { pair, exchangeMap ->
            exchangeMap.forEach { exchange, hopTicker ->

                for (windowDuration in windowDurations) {
                    val ticker = tickers
                            .getOrPut(pair, { mutableMapOf() })
                            .getOrPut(exchange, { mutableMapOf() })
                            .getOrPut(windowDuration, {
                                Ticker(pair, windowDuration, exchange,
                                        Date(closestSmallerMultiply(currentMillis, windowDuration) + windowDuration),
                                        Date(closestSmallerMultiply(currentMillis, windowDuration))
                                )
                            })
                    val window = windows
                            .getOrPut(pair, { mutableMapOf() })
                            .getOrPut(exchange, { mutableMapOf() })
                            .getOrPut(windowDuration, { LinkedList() })

                    window.offer(hopTicker)
                    ticker addHop hopTicker
                }
            }
        }
    }

    private fun updateVolumes(hopTickers: MutableMap<TokensPair, MutableMap<String, Ticker>>,
                              volumes: MutableMap<String, MutableMap<String, MutableMap<Long, TokenVolume>>>,
                              currentMillis: Long) {

        hopTickers.forEach { pair, exchangeMap ->
            exchangeMap.forEach { exchange, hopTicker ->

                for (windowDuration in windowDurations) {

                    val volumeFromBase = volumes
                            .getOrPut(pair.base, { mutableMapOf() })
                            .getOrPut(exchange, { mutableMapOf() })
                            .getOrPut(windowDuration, {
                                TokenVolume(pair.base, windowDuration, exchange, BigDecimal.ZERO,
                                        Date(closestSmallerMultiply(currentMillis, windowDuration)),
                                        Date(closestSmallerMultiply(currentMillis, windowDuration) + windowDuration)
                                )
                            })

                    val volumeFromQuote = volumes
                            .getOrPut(pair.quote, { mutableMapOf() })
                            .getOrPut(exchange, { mutableMapOf() })
                            .getOrPut(windowDuration, {
                                TokenVolume(pair.quote, windowDuration, exchange, BigDecimal.ZERO,
                                        Date(closestSmallerMultiply(currentMillis, windowDuration)),
                                        Date(closestSmallerMultiply(currentMillis, windowDuration) + windowDuration)
                                )
                            })

                    volumeFromBase.value = volumeFromBase.value.plus(hopTicker.baseAmount)
                    volumeFromQuote.value = volumeFromQuote.value.plus(hopTicker.quoteAmount)
                }
            }
        }
    }

    private fun cleanup(tickers: MutableMap<TokensPair, MutableMap<String, MutableMap<Long, Ticker>>>,
                        volumes: MutableMap<String, MutableMap<String, MutableMap<Long, TokenVolume>>>,
                        windows: MutableMap<TokensPair, MutableMap<String, MutableMap<Long, Queue<Ticker>>>>) {

        tickers.forEach { pair, exchangeMap ->
            exchangeMap.forEach { exchange, windowDurMap ->

                val iterator = windowDurMap.iterator()
                while (iterator.hasNext()) {
                    val mapEntry = iterator.next()

                    val windowDuration = mapEntry.key
                    val ticker = mapEntry.value

                    val window = windows[pair]!![exchange]!![windowDuration]
                    val volumeBase = volumes[pair.base]!![exchange]!![windowDuration]
                    val volumeQuote = volumes[pair.quote]!![exchange]!![windowDuration]

                    cleanupOne(ticker, volumeBase!!, volumeQuote!!, window!!)

                    if (window.isEmpty()) {
                        iterator.remove()
                    }
                }
            }
        }
    }

    private fun cleanupOne(ticker: Ticker, volumeBase: TokenVolume, volumeQuote: TokenVolume, window: Queue<Ticker>) {

        while (window.peek() != null && window.peek().timestampTo!!.time <= ticker.timestampFrom!!.time) {
            ticker minusHop window.peek()
            volumeBase.value = volumeBase.value.minus(window.peek().baseAmount)
            volumeQuote.value = volumeQuote.value.minus(window.peek().quoteAmount)

            window.poll()
        }

        if (!window.isEmpty()) {
            ticker.open = window.peek().open
            findMinMaxPrice(window, ticker)
        }
    }

    private fun findMinMaxPrice(window: Queue<Ticker>, ticker: Ticker) {
        var min = window.peek().minPrice
        var max = window.peek().maxPrice

        for (hopTicker in window) {
            min = min.min(hopTicker.minPrice)
            max = max.max(hopTicker.maxPrice)
        }

        ticker.minPrice = min
        ticker.maxPrice = max
    }

    private fun calculatePrice(tickers: MutableMap<TokensPair, MutableMap<String, MutableMap<Long, Ticker>>>,
                               volumes: MutableMap<String, MutableMap<String, MutableMap<Long, TokenVolume>>>) {

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
    }

    private fun updateTimestamps(tickers: MutableMap<TokensPair, MutableMap<String, MutableMap<Long, Ticker>>>,
                                 volumes: MutableMap<String, MutableMap<String, MutableMap<Long, TokenVolume>>>,
                                 currentMillisHop: Long) {
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

        volumes.forEach { token, exchangeMap ->
            exchangeMap.forEach { exchange, intervalMap ->
                intervalMap.forEach { interval, volume ->
                    if (volume.timestampTo.time <= currentMillisHop) {
                        val diff = currentMillisHop - volume.timestampTo.time + windowHop
                        volume.timestampFrom = Date(volume.timestampFrom.time + diff)
                        volume.timestampTo = Date(volume.timestampTo.time + diff)
                    }
                }
            }
        }
    }

    private fun sleep(windowHop: Long) {
        val currentMillisHop = closestSmallerMultiply(System.currentTimeMillis(), windowHop)
        val diff = currentMillisHop + windowHop - System.currentTimeMillis()
        log.debug("Time for hop calculation: {} ms", windowHop-diff)
        TimeUnit.MILLISECONDS.sleep(diff)
    }

    private fun log(tickers: MutableMap<TokensPair, MutableMap<String, MutableMap<Long, Ticker>>>, currentMillisHop: Long) {
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