package fund.cyber.markets.rest.task

import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit


class TaskExecutor(
        private val scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(1)
) {

    fun start() {
        scheduler.scheduleAtFixedRate(SupplyResolverTask(), 0, 1, TimeUnit.HOURS)
    }

}