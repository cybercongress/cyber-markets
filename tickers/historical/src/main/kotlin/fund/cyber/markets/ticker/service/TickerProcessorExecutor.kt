package fund.cyber.markets.ticker.service

import fund.cyber.markets.ticker.processor.HistoricalTickerProcessor
import org.springframework.stereotype.Component
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

@Component
class TickerProcessorExecutor(
    private val tickerProcessor: HistoricalTickerProcessor,
    private val lagFromRealTime: Long
) {
    private val scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(1)

    fun start() {
        scheduler.scheduleAtFixedRate({ tickerProcessor.start() }, 0L, lagFromRealTime, TimeUnit.MILLISECONDS)
    }

}