package fund.cyber.markets.ticker.processor

import fund.cyber.markets.common.Durations
import fund.cyber.markets.helpers.closestSmallerMultiply
import fund.cyber.markets.helpers.closestSmallerMultiplyFromTs
import fund.cyber.markets.model.BaseTokens
import fund.cyber.markets.model.Exchanges
import fund.cyber.markets.model.TokenPrice
import fund.cyber.markets.model.TokenTicker
import fund.cyber.markets.ticker.common.addHop
import fund.cyber.markets.ticker.common.minusHop
import fund.cyber.markets.ticker.configuration.TickersConfiguration
import fund.cyber.markets.ticker.service.TickerService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * @param tickers - map of TokenSymbol -> WindowDuration -> TokenTicker
 * @param windows - map of TokenSymbol -> WindowDuration -> Queue<TokenTicker>
 */
@Component
class TickerProcessor(
        val tickers: MutableMap<String, MutableMap<Long, TokenTicker>> = mutableMapOf(),
        val windows: MutableMap<String, MutableMap<Long, Queue<TokenTicker>>> = mutableMapOf()
) : TickerProcessorInterface {

    private val log = LoggerFactory.getLogger(TickerProcessor::class.java)!!

    @Autowired lateinit var hopTickerProcessor: HopTickerProcessor
    @Autowired lateinit var tickerService: TickerService
    @Autowired lateinit var configuration: TickersConfiguration

    fun process() {
        sleep(configuration.windowHop)
        while (true) {
            hopTickerProcessor.update()
            update(hopTickerProcessor.hopTickers)
            sleep(configuration.windowHop)
        }
    }

    fun update(hopTickers: MutableMap<String, TokenTicker>) {
        hopTickers.forEach { tokenSymbol, hopTicker ->
            configuration.windowDurations.forEach { duration ->

                val ticker = getTicker(tokenSymbol, duration)
                val window = getWindow(tokenSymbol, duration)

                window.offer(hopTicker)
                ticker addHop hopTicker
            }
        }

        cleanupOldData()
        calculateWeightedPrice()

        saveAndProduceToKafka()
        updateTimestamps()
    }

    private fun cleanupOldData() {
        tickers.forEach { tokenSymbol, windowDurationMap ->
            windowDurationMap.forEach { windowDuration, ticker ->

                val window = windows[tokenSymbol]!![windowDuration]!!
                while (window.isNotEmpty() && window.peek().timestampTo <= ticker.timestampFrom) {
                    ticker minusHop window.poll()
                }
            }
        }
    }

    private fun calculateWeightedPrice() {
        val baseTokensList = BaseTokens.values().map { it.name }

        tickers.forEach { tokenSymbol, windowDurationMap ->
            windowDurationMap.forEach { windowDuration, ticker ->
                baseTokensList.forEach { baseTokenSymbol ->

                    if (tokenSymbol != baseTokenSymbol) {

                        val volumes = windowDurationMap[Durations.DAY]?.volume?.get(baseTokenSymbol)
                        if (volumes != null) {

                            var tokenVolume24h = BigDecimal.ZERO
                            volumes.forEach { exchange, volumeValue ->
                                if (exchange != Exchanges.ALL) {
                                    tokenVolume24h = tokenVolume24h.plus(volumeValue)
                                }
                            }

                            val weightMap = mutableMapOf<String, BigDecimal>()
                            volumes.forEach { exchange, volumeValue ->
                                if (exchange != Exchanges.ALL
                                        && volumeValue.compareTo(BigDecimal.ZERO) == 1) {
                                    weightMap[exchange] = volumeValue.div(tokenVolume24h)
                                }
                            }

                            var avgPrice = BigDecimal.ZERO
                            weightMap.forEach { exchange, weight ->
                                val exchangePrice = ticker.price[baseTokenSymbol]!![exchange]!!.value!!.multiply(weight)
                                if (exchangePrice != null) {
                                    avgPrice = avgPrice.plus(exchangePrice)
                                }
                            }

                            ticker.price[baseTokenSymbol]!!.put(Exchanges.ALL, TokenPrice(avgPrice))
                        }
                    } else {
                        ticker.price[baseTokenSymbol]!!.put(Exchanges.ALL, TokenPrice(BigDecimal.ONE))
                    }
                }
            }
        }
    }

    fun saveAndProduceToKafka() {
        tickerService.saveAndProduceToKafka(tickers)
    }

    fun updateTimestamps() {
        //todo: correct timestamp ?
        val currentMillisHop = closestSmallerMultiplyFromTs(configuration.windowHop)

        tickers.forEach { _, windowDurationMap ->
            windowDurationMap.forEach { _, ticker ->
                if (ticker.timestampTo <= currentMillisHop) {
                    val difference = currentMillisHop - ticker.timestampTo + configuration.windowHop
                    ticker.timestampFrom += difference
                    ticker.timestampTo += difference
                }
            }
        }
    }

    private fun getTicker(tokenSymbol: String, windowDuration: Long): TokenTicker {
        //todo: correct timestamp ?
        val timestampFrom = closestSmallerMultiplyFromTs(windowDuration)

        return tickers
                .getOrPut(tokenSymbol, { mutableMapOf() })
                .getOrPut(windowDuration, {
                    TokenTicker(
                            symbol = tokenSymbol,
                            timestampFrom = timestampFrom,
                            timestampTo = timestampFrom + windowDuration,
                            interval = windowDuration)
                })
    }

    private fun getWindow(tokenSymbol: String, windowDuration: Long): Queue<TokenTicker> {
        return windows
                .getOrPut(tokenSymbol, { mutableMapOf() })
                .getOrPut(windowDuration, { LinkedList<TokenTicker>() })
    }

    private fun sleep(windowHop: Long) {
        val currentMillisHop = closestSmallerMultiply(System.currentTimeMillis(), windowHop)
        val diff = currentMillisHop + windowHop - System.currentTimeMillis()
        log.debug("Time for hop calculation: {} ms", windowHop - diff)
        TimeUnit.MILLISECONDS.sleep(diff)
    }

}