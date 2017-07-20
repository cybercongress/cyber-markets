package fund.cyber.markets.service

import fund.cyber.markets.model.ConnectionWithExchangeIsLost
import fund.cyber.markets.model.ConnectionWithExchangeIsReestablished
import fund.cyber.markets.model.HistoryGap
import fund.cyber.markets.storage.RethinkDbService
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap


@Component
open class HistoryDataGapsService(
        val rethinkDbService: RethinkDbService
) {

    private val LOG = LoggerFactory.getLogger(HistoryDataGapsService::class.java)

    private val exchangesWithLostConnection: MutableMap<String, HistoryGap> = ConcurrentHashMap()

    @Async
    @EventListener
    open fun handleConnectionWithExchangeLostEvent(event: ConnectionWithExchangeIsLost) {

        // already known event. should not occur, but one more check
        if (exchangesWithLostConnection.contains(event.exchange)) {
            return
        }

        val openHistoryGap = HistoryGap(exchange = event.exchange, startTime = event.time)
        exchangesWithLostConnection.put(event.exchange, openHistoryGap)
        rethinkDbService.saveHistoryGap(openHistoryGap)
    }

    @Async
    @EventListener
    open fun handleConnectionWithExchangeReestablishedEvent(event: ConnectionWithExchangeIsReestablished) {

        val historyGap = exchangesWithLostConnection[event.exchange]

        if (historyGap == null) {
            LOG.error("Unknown reconnection event to ${event.exchange} exchange")
            return
        }

        exchangesWithLostConnection.remove(event.exchange)
        historyGap.endTime = event.time
        rethinkDbService.saveHistoryGap(historyGap)
    }
}
