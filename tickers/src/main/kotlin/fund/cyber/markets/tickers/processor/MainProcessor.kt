package fund.cyber.markets.tickers.processor

import fund.cyber.markets.common.closestSmallerMultiply
import fund.cyber.markets.tickers.AppContext
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

class MainProcessor(
        private val windowHop: Long = AppContext.configuration.windowHop,
        private val hopTickerProcessor: HopTickerProcessor = HopTickerProcessor(),
        private val tickerProcessor: TickerProcessor = TickerProcessor(),
        private val volumeProcessor: VolumeProcessor = VolumeProcessor()
) {

    private val log = LoggerFactory.getLogger(MainProcessor::class.java)!!

    fun process() {

        sleep(windowHop)
        while (true) {

            hopTickerProcessor.poll()
            val currentMillis = System.currentTimeMillis()
            val currentMillisHop = closestSmallerMultiply(currentMillis, windowHop)

            hopTickerProcessor.update(currentMillisHop)

            tickerProcessor.update(hopTickerProcessor, currentMillis)
            volumeProcessor.update(hopTickerProcessor, tickerProcessor, currentMillis)
            tickerProcessor.cleanupWindows()

            tickerProcessor.calculatePrice(volumeProcessor, currentMillisHop)

            tickerProcessor.saveAndProduceToKafka(currentMillisHop)
            volumeProcessor.saveAndProduceToKafka(currentMillisHop)

            tickerProcessor.updateTimestamps(currentMillisHop)
            volumeProcessor.updateTimestamps(currentMillisHop)

            sleep(windowHop)
        }

    }

    private fun sleep(windowHop: Long) {
        val currentMillisHop = closestSmallerMultiply(System.currentTimeMillis(), windowHop)
        val diff = currentMillisHop + windowHop - System.currentTimeMillis()
        log.debug("Time for hop calculation: {} ms", windowHop-diff)
        TimeUnit.MILLISECONDS.sleep(diff)
    }

}