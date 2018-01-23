package fund.cyber.markets.tickers

import fund.cyber.markets.cassandra.repository.TickerRepository
import fund.cyber.markets.common.Durations
import fund.cyber.markets.dto.TokensPair
import fund.cyber.markets.model.Ticker
import fund.cyber.markets.model.TickerKey
import fund.cyber.markets.model.Trade
import fund.cyber.markets.tickers.configuration.TickersConfiguration
import io.reactivex.schedulers.Schedulers
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.TopicPartition
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.math.RoundingMode
import java.sql.Timestamp
import java.util.*
import java.util.concurrent.TimeUnit

class TickersProcessor(
        val configuration: TickersConfiguration,
        val consumer: KafkaConsumer<String, Trade>,
        val consumerBackup: KafkaConsumer<TickerKey, Ticker>,
        val producer: KafkaProducer<TickerKey, Ticker>,
        val tickersRepository: TickerRepository,
        private val windowHop: Long = configuration.windowHop,
        private val windowDurations: List<Long> = configuration.windowDurations
) {

    private val log = LoggerFactory.getLogger(TickersProcessor::class.java)!!
    private val thread = Schedulers.io()

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
     *   See {@link #cleanupTickers(MutableMap<TokensPair, MutableMap<String, MutableMap<Long, Ticker>>>,
     *                              MutableMap<TokensPair, MutableMap<String, MutableMap<Long, Queue<Ticker>>>>)
     *   cleanupTickers} method
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
     *   See {@link updateTickerTimestamps(MutableMap<TokensPair, MutableMap<String, MutableMap<Long, Ticker>>>, Long)
     *   updateTickerTimestamps} method
     */

    fun process() {
        consumer.subscribe(configuration.topicNamePattern)
        consumerBackup.subscribe(listOf(configuration.tickersBackupTopicName))
        seekToEnd()

        val hopTickers = mutableMapOf<TokensPair, MutableMap<String, Ticker>>()
        val tickers = mutableMapOf<TokensPair, MutableMap<String, MutableMap<Long, Ticker>>>()
        val windows = mutableMapOf<TokensPair, MutableMap<String, MutableMap<Long, Queue<Ticker>>>>()

        sleep(windowHop)
        while (true) {

            val currentMillis = System.currentTimeMillis()
            val currentMillisHop = currentMillis / windowHop * windowHop
            val records = consumer.poll(Long.MAX_VALUE)
            log.debug("Trades count: {}", records.count())

            calculateHopTickers(records, hopTickers, currentMillisHop)

            updateTickers(hopTickers, tickers, windows, currentMillis)

            cleanupTickers(tickers, windows)

            calculatePrice(tickers)

            if (log.isTraceEnabled) {
                log(tickers, currentMillisHop)
            }

            saveAndProduceToKafka(tickers, configuration.tickersTopicName, currentMillisHop)

            updateTickerTimestamps(tickers, currentMillisHop)

            hopTickers.clear()

            sleep(windowHop)
        }

    }

    private fun calculateHopTickers(records: ConsumerRecords<String, Trade>,
                                    hopTickers: MutableMap<TokensPair, MutableMap<String, Ticker>>,
                                    currentMillisHop: Long) {

        var droppedCount = 0
        for (record in records) {
            val currentMillisHopFrom = currentMillisHop - windowHop * 2
            if (record.timestamp() < currentMillisHopFrom) {
                droppedCount++
                continue
            }

            val trade = record.value()
            val ticker = hopTickers
                    .getOrPut(trade.pair, { mutableMapOf() })
                    .getOrPut(trade.exchange, {
                        Ticker(windowHop)
                                .setTimestamps(
                                        currentMillisHopFrom,
                                        currentMillisHop
                                )
                    })
            val tickerAllExchange = hopTickers
                    .getOrPut(trade.pair, { mutableMapOf() })
                    .getOrPut("ALL", {
                        Ticker(windowHop)
                                .setTimestamps(
                                        currentMillisHopFrom,
                                        currentMillisHop
                                )
                    })
            ticker.add(trade)
            tickerAllExchange.add(trade).setExchangeString("ALL")
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
                                Ticker(windowDuration).setTimestamps(
                                        currentMillis / windowDuration * windowDuration,
                                        currentMillis / windowDuration * windowDuration + windowDuration)
                            })
                    val window = windows
                            .getOrPut(pair, { mutableMapOf() })
                            .getOrPut(exchange, { mutableMapOf() })
                            .getOrPut(windowDuration, { LinkedList() })

                    window.offer(hopTicker)
                    ticker.add(hopTicker)
                }
            }
        }
    }

    private fun cleanupTickers(tickers: MutableMap<TokensPair, MutableMap<String, MutableMap<Long, Ticker>>>,
                                windows: MutableMap<TokensPair, MutableMap<String, MutableMap<Long, Queue<Ticker>>>>) {

        tickers.forEach { pair, exchangeMap ->
            exchangeMap.forEach { exchange, windowDurMap ->

                val iterator = windowDurMap.iterator()
                while (iterator.hasNext()) {
                    val mapEntry = iterator.next()

                    val windowDuration = mapEntry.key
                    val ticker = mapEntry.value
                    val window = windows[pair]!![exchange]!![windowDuration]

                    cleanUpTicker(window!!, ticker)

                    if (window.isEmpty()) {
                        iterator.remove()
                    }
                }
            }
        }
    }

    private fun calculatePrice(tickers: MutableMap<TokensPair, MutableMap<String, MutableMap<Long, Ticker>>>) {

        windowDurations.forEach { windowDuration ->
            tickers.forEach { pair, exchangeMap ->

                var totalQuoteAmount = BigDecimal(0)
                exchangeMap.forEach { exchange, windowDurMap ->
                    if (exchange != "ALL") {
                        val ticker = windowDurMap[Durations.DAY]
                        if (ticker != null) {
                            totalQuoteAmount = totalQuoteAmount.add(ticker.quoteAmount)
                        }
                    }
                }

                val weightMap = mutableMapOf<String, BigDecimal>()
                exchangeMap.forEach { exchange, windowDurMap ->
                    if (exchange != "ALL") {
                        val ticker = windowDurMap[Durations.DAY]
                        if (ticker != null) {
                            weightMap.put(exchange, ticker.quoteAmount.divide(totalQuoteAmount, RoundingMode.HALF_UP))
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

    private fun saveAndProduceToKafka(tickers: MutableMap<TokensPair, MutableMap<String, MutableMap<Long, Ticker>>>, topicName: String, currentMillisHop: Long) {
        val tickerSnapshots = mutableListOf<Ticker>()

        producer.beginTransaction()
        try {
            tickers.forEach { pair, exchangeMap ->
                exchangeMap.forEach { exchange, windowDurMap ->
                    windowDurMap.forEach { windowDuration, ticker ->
                        if (configuration.allowNotClosedWindows) {
                            producer.send(produceRecord(ticker, topicName))
                        } else if (ticker.timestampTo!!.time <= currentMillisHop) {
                            producer.send(produceRecord(ticker, topicName))
                        }
                        if (ticker.timestampTo!!.time <= currentMillisHop) {
                            if (isSnapshot(ticker, windowDuration)) {
                                tickerSnapshots.add(ticker)
                            }
                        }
                    }
                }
            }
        } catch (e : Exception) {
            producer.abortTransaction()
            Runtime.getRuntime().exit(-1)
        }
        producer.commitTransaction()

        if (tickerSnapshots.isNotEmpty()) {
            saveSnapshots(tickerSnapshots)
        }
    }

    private fun saveSnapshots(tickerSnapshots: MutableList<Ticker>) {
        log.debug("Save snapshots")

        val snapshots = mutableListOf<Ticker>()
        tickerSnapshots.forEach {
            snapshots.add(it.copy())
        }

        thread.scheduleDirect {
            snapshots.forEach { ticker ->
                try {
                    tickersRepository.save(ticker)
                } catch (e: Exception) {
                    backupTickerToKafka(ticker)
                }
            }
        }

        restoreTickersFromKafka()
    }

    private fun backupTickerToKafka(ticker: Ticker) {
        log.debug("Backuping ticker to kafka: {}", ticker)

        producer.beginTransaction()
        try {
            producer.send(produceRecord(ticker, configuration.tickersBackupTopicName))
        } catch (e: Exception) {
            producer.abortTransaction()
            Runtime.getRuntime().exit(-1)
        }
        producer.commitTransaction()
    }

    private fun restoreTickersFromKafka() {
        log.debug("Restore tickers from kafka")

        Schedulers.single().scheduleDirect {
            val records: ConsumerRecords<TickerKey, Ticker> = consumerBackup.poll(Long.MAX_VALUE)
            log.debug("Tickers for restore count: {}", records.count())

            records.forEach { record ->
                try {
                    tickersRepository.save(record.value())
                } catch (e: Exception) {
                    log.debug("Restore failed: {} ", record.value())
                }
            }
        }
    }

    private fun isSnapshot(ticker: Ticker, windowDuration: Long): Boolean {
        return ticker.timestampTo!!.time % windowDuration == 0L
    }

    private fun produceRecord(ticker: Ticker, topicName: String): ProducerRecord<TickerKey, Ticker> {
        return ProducerRecord(
                topicName,
                TickerKey(ticker.pair!!, ticker.windowDuration, Timestamp(ticker.timestampFrom!!.time)),
                ticker)
    }

    private fun updateTickerTimestamps(tickers: MutableMap<TokensPair, MutableMap<String, MutableMap<Long, Ticker>>>, currentMillisHop: Long) {
        tickers.forEach { pair, exchangeMap ->
            exchangeMap.forEach { exchange, windowDurMap ->
                windowDurMap.forEach { windowDuration, ticker ->
                    if (ticker.timestampTo!!.time <= currentMillisHop) {
                        val diff = currentMillisHop - ticker.timestampTo!!.time + windowHop
                        ticker.setTimestamps(
                                ticker.timestampFrom!!.time + diff,
                                ticker.timestampTo!!.time + diff)
                    }
                }
            }
        }
    }

    private fun cleanUpTicker(window: Queue<Ticker>, ticker: Ticker) {
        while (window.peek() != null && window.peek().timestampTo!!.time <= ticker.timestampFrom!!.time) {
            ticker.minus(window.poll())
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
            min = min?.min(hopTicker.minPrice)
            max = max?.max(hopTicker.maxPrice)
        }

        ticker.minPrice = min
        ticker.maxPrice = max
    }

    private fun seekToEnd() {
        consumer.poll(0)
        val partitions = mutableListOf<TopicPartition>()
        val tradeTopics = consumer.subscription()
        tradeTopics.forEach { topic ->
            consumer.partitionsFor(topic).forEach { partitionInfo ->
                partitions.add(TopicPartition(partitionInfo.topic(), partitionInfo.partition()))
            }
        }
        consumer.seekToEnd(partitions)
    }

    private fun sleep(windowHop: Long) {
        val currentMillisHop = System.currentTimeMillis() / windowHop * windowHop
        val diff = currentMillisHop + windowHop - System.currentTimeMillis()
        log.debug("Time for hop calculation: {} ms", windowHop-diff)
        TimeUnit.MILLISECONDS.sleep(diff)
    }

    private fun log(tickers: MutableMap<TokensPair, MutableMap<String, MutableMap<Long, Ticker>>>, currentMillisHop: Long) {
        log.trace("Window timestamp: {}", Timestamp(currentMillisHop))
        tickers.forEach { pair, exchangeMap ->
            exchangeMap.forEach { exchange, windowDurMap ->
                windowDurMap.forEach { windowDuration, ticker ->
                    if (ticker.timestampTo!!.time <= currentMillisHop) {
                        log.trace(ticker.toString())
                    }
                }
            }
        }
    }

}