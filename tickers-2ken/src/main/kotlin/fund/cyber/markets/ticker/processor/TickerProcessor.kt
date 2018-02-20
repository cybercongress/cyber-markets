package fund.cyber.markets.ticker.processor

import fund.cyber.markets.common.Durations
import fund.cyber.markets.helpers.closestSmallerMultiplyFromTs
import fund.cyber.markets.model.BaseTokens
import fund.cyber.markets.model.Exchanges
import fund.cyber.markets.model.Ticker
import fund.cyber.markets.model.TokenPrice
import fund.cyber.markets.model.TokenTicker
import fund.cyber.markets.model.TokensPair
import fund.cyber.markets.ticker.common.addHop
import fund.cyber.markets.ticker.common.minusHop
import fund.cyber.markets.ticker.configuration.TickersConfiguration
import fund.cyber.markets.ticker.service.TickerService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.sql.Timestamp
import java.util.*

/**
 * @param newTickers - TokenSymbol -> WindowDuration -> TokenTicker
 * @param newWindows - TokenSymbol -> WindowDuration -> Queue<TokenTicker>
 */
@Component
class TickerProcessor(
        val newTickers: MutableMap<String, MutableMap<Long, TokenTicker>> = mutableMapOf(),
        val newWindows: MutableMap<String, MutableMap<Long, Queue<TokenTicker>>> = mutableMapOf(),

        val tickers: MutableMap<TokensPair, MutableMap<String, MutableMap<Long, Ticker>>> = mutableMapOf(),
        val windows: MutableMap<TokensPair, MutableMap<String, MutableMap<Long, Queue<Ticker>>>> = mutableMapOf()
) : TickerProcessorInterface {

    private val log = LoggerFactory.getLogger(TickerProcessor::class.java)!!

    @Autowired
    lateinit var configuration: TickersConfiguration

    private val windowHop: Long by lazy { configuration.windowHop }
    private val windowDurations: Set<Long> by lazy { configuration.windowDurations }

    @Autowired
    lateinit var tickerService: TickerService

    fun update(hopTickers: MutableMap<String, TokenTicker>) {
        hopTickers.forEach { tokenSymbol, hopTicker ->
            windowDurations.forEach { duration ->

                val ticker = getTicker(tokenSymbol, duration)
                val window = getWindow(tokenSymbol, duration)

                window.offer(hopTicker)
                ticker addHop hopTicker
            }
        }

        cleanupOldData()
        calculateWeightedPrice()

        println("next hop")
    }

    private fun cleanupOldData() {
        newTickers.forEach { tokenSymbol, windowDurationMap ->
            windowDurationMap.forEach { windowDuration, ticker ->

                val window = newWindows[tokenSymbol]!![windowDuration]!!
                while (window.isNotEmpty() && window.peek().timestampTo <= ticker.timestampFrom) {
                    ticker minusHop window.poll()
                }
            }
        }
    }

    private fun calculateWeightedPrice() {
        val baseTokensList = BaseTokens.values().map { it.name }

        newTickers.forEach { tokenSymbol, windowDurationMap ->
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

                            newTickers[tokenSymbol]!![windowDuration]!!.price[baseTokenSymbol]!!
                                    .put(Exchanges.ALL, TokenPrice(avgPrice))

                        }
                    }
                }
            }
        }
    }

    private fun getTicker(tokenSymbol: String, windowDuration: Long): TokenTicker {
        //todo: correct timestamp ?
        val timestampFrom = closestSmallerMultiplyFromTs(windowDuration)

        return newTickers
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
        return newWindows
                .getOrPut(tokenSymbol, { mutableMapOf() })
                .getOrPut(windowDuration, { LinkedList<TokenTicker>() })
    }

    fun updateTimestamps(currentMillisHop: Long) {
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

    fun saveAndProduceToKafka(currentMillisHop: Long) {
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