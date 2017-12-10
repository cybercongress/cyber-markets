package fund.cyber.markets.tickers

import fund.cyber.markets.dao.service.TickerDaoService
import fund.cyber.markets.dto.TokensPair
import fund.cyber.markets.model.Trade
import fund.cyber.markets.tickers.configuration.TickersConfiguration
import fund.cyber.markets.model.Ticker
import fund.cyber.markets.model.TickerKey
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import java.math.BigDecimal
import java.math.RoundingMode
import java.sql.Timestamp
import java.util.*
import java.util.concurrent.TimeUnit

class TickersProcessor(
        val configuration: TickersConfiguration,
        val consumer: KafkaConsumer<String, Trade>,
        val producer: KafkaProducer<TickerKey, Ticker>,
        val tickersDaoService: TickerDaoService,
        private val windowHop: Long = configuration.windowHop,
        private val windowDurations: List<Long> = configuration.windowDurations
) {

    fun process() {
        consumer.subscribe(configuration.topicNamePattern)

        val hopTickers = mutableMapOf<TokensPair, MutableMap<String, Ticker>>()
        val tickers = mutableMapOf<TokensPair, MutableMap<String, MutableMap<Long, Ticker>>>()
        val windows = mutableMapOf<TokensPair, MutableMap<String, MutableMap<Long, Queue<Ticker>>>>()

        sleep(windowHop)
        while (true) {
            val records = consumer.poll(windowHop / 2)
            val currentMillis = System.currentTimeMillis()
            val currentMillisHop = currentMillis / windowHop * windowHop

            //update hop_tickers
            records.forEach { record ->
                val trade = record.value()
                val ticker = hopTickers
                        .getOrPut(trade.pair, { mutableMapOf() })
                        .getOrPut(trade.exchange, {
                            Ticker(windowHop).setTimestamps(
                                    currentMillisHop,
                                    currentMillisHop + windowHop)
                        })
                val tickerAllExchange = hopTickers
                        .getOrPut(trade.pair, { mutableMapOf() })
                        .getOrPut("ALL", {
                            Ticker(windowHop)
                                    .setExchangeString("ALL")
                                    .setTimestamps(
                                            currentMillisHop,
                                            currentMillisHop + windowHop)
                        })
                ticker.add(trade)
                tickerAllExchange.add(trade)
            }

            //add hop_tickers to tickers
            hopTickers.forEach { tokensPair, exchangeMap ->
                exchangeMap.forEach { exchange, hopTicker ->

                    for (windowDuration in windowDurations) {
                        val ticker = tickers
                                .getOrPut(tokensPair, { mutableMapOf() })
                                .getOrPut(exchange, { mutableMapOf() })
                                .getOrPut(windowDuration, {
                                    Ticker(windowDuration).setTimestamps(
                                            currentMillis / windowDuration * windowDuration,
                                            currentMillis / windowDuration * windowDuration + windowDuration)
                                })
                        val window = windows
                                .getOrPut(tokensPair, { mutableMapOf() })
                                .getOrPut(exchange, { mutableMapOf() })
                                .getOrPut(windowDuration, { LinkedList() })

                        window.offer(hopTicker)
                        ticker.add(hopTicker)
                    }
                }
            }

            //cleanup old tickers
            tickers.forEach { tokensPair, exchangeMap ->
                exchangeMap.forEach { exchange, windowDurMap ->

                    val iterator = windowDurMap.iterator()
                    while (iterator.hasNext()) {
                        val mapEntry = iterator.next()

                        val windowDuration = mapEntry.key
                        val ticker = mapEntry.value
                        val window = windows[tokensPair]!![exchange]!![windowDuration]

                        cleanUpTicker(window!!, ticker)

                        //remove window without hop_tickers
                        if (window.isEmpty()) {
                            iterator.remove()
                        }
                    }
                }
            }

            //calc price
            calculatePrice(tickers)

            //log
            if (configuration.debug) {
                log(tickers, currentMillisHop)
            }

            //save and produce to kafka
            saveAndProduceToKafka(tickers, configuration.tickersTopicName, currentMillisHop)

            //update timestamps
            tickers.forEach { tokensPair, exchangeMap ->
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

            hopTickers.clear()

            //sleep
            sleep(windowHop)
        }

    }

    private fun calculatePrice(tickers: MutableMap<TokensPair, MutableMap<String, MutableMap<Long, Ticker>>>) {

        windowDurations.forEach { windowDuration ->
            tickers.forEach { tokensPair, exchangeMap ->

                var sumPrice = BigDecimal(0)
                exchangeMap.forEach { exchange, windowDurMap ->
                    if (exchange != "ALL") {
                        val ticker = windowDurMap[windowDuration]
                        if (ticker != null) {
                            sumPrice = sumPrice.add(ticker.calcPrice().price)
                        }
                    }
                }

                val weightMap = mutableMapOf<String, BigDecimal>()
                exchangeMap.forEach { exchange, windowDurMap ->
                    if (exchange != "ALL") {
                        val ticker = windowDurMap[windowDuration]
                        if (ticker != null) {
                            weightMap.put(exchange, ticker.calcPrice().price.divide(sumPrice, RoundingMode.HALF_UP))
                        }
                    }
                }

                var avgPrice = BigDecimal(0)
                weightMap.forEach { exchange, weight ->
                    val ticker = exchangeMap[exchange]?.get(windowDuration)
                    if (ticker != null) {
                        val weightedPrice = ticker.price.multiply(weight)
                        avgPrice = avgPrice.plus(weightedPrice)
                    }
                }

                val tickerAllExchange = exchangeMap["ALL"]?.get(windowDuration)
                if (tickerAllExchange != null) {
                    tickerAllExchange.price = avgPrice
                }
            }
        }
    }

    private fun saveAndProduceToKafka(tickers: MutableMap<TokensPair, MutableMap<String, MutableMap<Long, Ticker>>>, topicName: String, currentMillisHop: Long) {
        producer.beginTransaction()
        try {
            tickers.forEach { tokensPair, exchangeMap ->
                exchangeMap.forEach { exchange, windowDurMap ->
                    windowDurMap.forEach { windowDuration, ticker ->
                        if (ticker.timestampTo!!.time <= currentMillisHop) {
                            producer.send(producerRecord(ticker, topicName))
                            saveSnapshot(ticker, windowDuration)
                        }
                    }
                }
            }
        } catch (e : Exception) {
            producer.abortTransaction()
            Runtime.getRuntime().exit(-1)
        }
        producer.commitTransaction()
    }

    private fun saveSnapshot(ticker: Ticker, windowDuration: Long) {
        if (ticker.timestampTo!!.time % windowDuration == 0L) {
            tickersDaoService.insert(ticker)
        }
    }

    private fun producerRecord(ticker: Ticker, topicName: String): ProducerRecord<TickerKey, Ticker> {
        return ProducerRecord(
                topicName,
                TickerKey(ticker.tokensPair!!, ticker.windowDuration, Timestamp(ticker.timestampFrom!!.time)),
                ticker)
    }

    private fun cleanUpTicker(window: Queue<Ticker>, ticker: Ticker) {
        while (window.peek() != null && window.peek().timestampTo!!.time <= ticker.timestampFrom!!.time) {
            ticker.minus(window.poll())
        }
    }

    private fun sleep(windowHop: Long) {
        val currentMillisHop = System.currentTimeMillis() / windowHop * windowHop
        val diff = currentMillisHop + windowHop - System.currentTimeMillis()
        TimeUnit.MILLISECONDS.sleep(diff)
    }

    private fun log(tickers: MutableMap<TokensPair, MutableMap<String, MutableMap<Long, Ticker>>>, currentMillisHop: Long) {
        println("Window timestamp: " + Timestamp(currentMillisHop))
        tickers.forEach { tokensPair, exchangeMap ->
            exchangeMap.forEach { exchange, windowDurMap ->
                windowDurMap.forEach { windowDuration, ticker ->
                    if (ticker.timestampTo!!.time <= currentMillisHop) {
                        println(ticker)
                    }
                }
            }
        }

    }

}