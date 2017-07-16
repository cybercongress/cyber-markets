package fund.cyber.markets.poloniex


import fund.cyber.markets.exchanges.common.TradesAndOrdersUpdatesMessage
import fund.cyber.markets.storage.RethinkDbService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.TaskScheduler
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.WebSocketMessage
import org.springframework.web.socket.WebSocketSession
import java.util.concurrent.ScheduledFuture


@Component
open class PoloniexWebSocketHandler(
        val taskScheduler: TaskScheduler,
        val poloniexMetaInformation: PoloniexMetaInformation,
        val poloniexMessageParser: PoloniexMessageParser,
        val rethinkDbService: RethinkDbService
) : WebSocketHandler {

    private val LOG = LoggerFactory.getLogger(PoloniexWebSocketHandler::class.java)
    private var scheduledTask: ScheduledFuture<*>? = null


    @Throws(Exception::class)
    override fun afterConnectionEstablished(session: WebSocketSession) {
        LOG.info("Poloniex websocket session is started")
        session.textMessageSizeLimit = Integer.MAX_VALUE
        val subscriptionsUpdater = PoloniexSubscriptionUpdater(poloniexMetaInformation, session)
        scheduledTask = taskScheduler.scheduleAtFixedRate(subscriptionsUpdater, 5 * 60 * 1000)
    }

    @Throws(Exception::class)
    override fun handleMessage(session: WebSocketSession, message: WebSocketMessage<*>) {
        val jsonMessage = message.payload.toString()
        val exchangeMessage = poloniexMessageParser.parseMessage(jsonMessage)
        when (exchangeMessage) {
            is TradesAndOrdersUpdatesMessage -> rethinkDbService.saveTrades(exchangeMessage.trades)
        }
    }

    @Throws(Exception::class)
    override fun handleTransportError(session: WebSocketSession, exception: Throwable) {
        LOG.info("Poloniex websocket transport error", exception)
    }

    @Throws(Exception::class)
    override fun afterConnectionClosed(session: WebSocketSession, closeStatus: CloseStatus) {
        LOG.info("Poloniex websocket session is closed")
        scheduledTask?.cancel(true)
    }

    override fun supportsPartialMessages(): Boolean {
        return false
    }
}
