package fund.cyber.markets.ticker.processor

import fund.cyber.markets.helpers.closestSmallerMultiply
import fund.cyber.markets.model.Ticker
import fund.cyber.markets.model.TokenVolume
import fund.cyber.markets.ticker.configuration.TickersConfiguration
import fund.cyber.markets.ticker.service.VolumeService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.util.*

@Component
class VolumeProcessor(
        val volumes: MutableMap<String, MutableMap<String, MutableMap<Long, TokenVolume>>> = mutableMapOf()
): VolumeProcessorInterface {

    private val log = LoggerFactory.getLogger(VolumeProcessor::class.java)!!

    @Autowired
    lateinit var configuration: TickersConfiguration

    private val windowHop: Long by lazy { configuration.windowHop }
    private val windowDurations: Set<Long> by lazy { configuration.windowDurations }

    @Autowired
    lateinit var volumeService: VolumeService

    override fun update(hopTickerProcessor: HopTickerProcessor, tickerProcessor: TickerProcessor, currentMillis: Long) {

/*        hopTickerProcessor.hopTickers.forEach { pair, exchangeMap ->
            exchangeMap.forEach { exchange, hopTicker ->
                for (windowDuration in windowDurations) {
                    val volumeFromBase = get(pair.base, exchange, windowDuration, currentMillis)
                    val volumeFromQuote = get(pair.quote, exchange, windowDuration, currentMillis)

                    volumeFromBase.value = volumeFromBase.value.plus(hopTicker.baseAmount)
                    volumeFromQuote.value = volumeFromQuote.value.plus(hopTicker.quoteAmount)
                }
            }
        }

        cleanupOldData(tickerProcessor)*/
    }

    override fun get(token: String, exchange: String, windowDuration: Long, currentMillis: Long): TokenVolume {
        return volumes
                .getOrPut(token, { mutableMapOf() })
                .getOrPut(exchange, { mutableMapOf() })
                .getOrPut(windowDuration, {
                    TokenVolume(token, windowDuration, exchange, BigDecimal.ZERO,
                            Date(closestSmallerMultiply(currentMillis, windowDuration)),
                            Date(closestSmallerMultiply(currentMillis, windowDuration) + windowDuration)
                    )
                })
    }

    override fun updateTimestamps(currentMillisHop: Long) {
        volumes.forEach { _, exchangeMap ->
            exchangeMap.forEach { _, intervalMap ->
                intervalMap.forEach { _, volume ->
                    if (volume.timestampTo.time <= currentMillisHop) {
                        val diff = currentMillisHop - volume.timestampTo.time + windowHop
                        volume.timestampFrom = Date(volume.timestampFrom.time + diff)
                        volume.timestampTo = Date(volume.timestampTo.time + diff)
                    }
                }
            }
        }
    }

    private fun cleanupOldData(tickerProcessor: TickerProcessor) {
        tickerProcessor.tickers.forEach { pair, exchangeMap ->
            exchangeMap.forEach { exchange, windowDurationMap ->
                windowDurationMap.forEach { windowDuration, ticker ->

                    val window = tickerProcessor.windows[pair]!![exchange]!![windowDuration]!!
                    val windowCopy = LinkedList<Ticker>()
                    window.forEach {
                        windowCopy.add(it.copy())
                    }

                    val volumeBase = volumes[pair.base]!![exchange]!![windowDuration]!!
                    val volumeQuote = volumes[pair.quote]!![exchange]!![windowDuration]!!

                    while (windowCopy.peek() != null &&
                            windowCopy.peek().timestampTo!!.time <= ticker.timestampFrom!!.time) {

                        volumeBase.value = volumeBase.value.minus(windowCopy.peek().baseAmount)
                        volumeQuote.value = volumeQuote.value.minus(windowCopy.peek().quoteAmount)

                        windowCopy.poll()
                    }

                }
            }
        }
    }

    override fun saveAndProduceToKafka(currentMillisHop: Long) {
        volumeService.saveAndProduceToKafka(volumes, currentMillisHop)
    }

}