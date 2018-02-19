package fund.cyber.markets.ticker.processor

import fund.cyber.markets.helpers.closestSmallerMultiply
import fund.cyber.markets.ticker.configuration.TickersConfiguration
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class MainProcessor {

    private val log = LoggerFactory.getLogger(MainProcessor::class.java)!!

    @Autowired
    lateinit var configuration: TickersConfiguration

    private val windowHop: Long by lazy { configuration.windowHop }

    @Autowired
    lateinit var hopTickerProcessor: HopTickerProcessor

    @Autowired
    lateinit var tickerProcessor: TickerProcessor

    @Autowired
    lateinit var volumeProcessor: VolumeProcessor

    fun process() {

        sleep(windowHop)
        while (true) {

/*            hopTickerProcessor.poll()
            val currentMillis = System.currentTimeMillis()
            val currentMillisHop = closestSmallerMultiply(currentMillis, windowHop)*/

            hopTickerProcessor.update()

/*            tickerProcessor.update(hopTickerProcessor, currentMillis)
            volumeProcessor.update(hopTickerProcessor, tickerProcessor, currentMillis)
            tickerProcessor.cleanupWindows()

            tickerProcessor.calculatePrice(volumeProcessor, currentMillisHop)

            tickerProcessor.saveAndProduceToKafka(currentMillisHop)
            volumeProcessor.saveAndProduceToKafka(currentMillisHop)

            tickerProcessor.updateTimestamps(currentMillisHop)
            volumeProcessor.updateTimestamps(currentMillisHop)*/

            sleep(windowHop)
        }

    }

    private fun sleep(windowHop: Long) {
        val currentMillisHop = closestSmallerMultiply(System.currentTimeMillis(), windowHop)
        val diff = currentMillisHop + windowHop - System.currentTimeMillis()
        log.debug("Time for hop calculation: {} ms", windowHop - diff)
        TimeUnit.MILLISECONDS.sleep(diff)
    }

}