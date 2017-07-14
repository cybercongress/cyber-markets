package fund.cyber.markets.poloniex

import org.knowm.xchange.ExchangeFactory
import org.knowm.xchange.poloniex.PoloniexExchange
import org.slf4j.LoggerFactory
import org.springframework.web.socket.WebSocketSession


/**
 * Updates poloniex meta information and ws subscription channels on regular basis.
 *
 * @author hleb.albau@gmail.com
 */
open class PoloniexSubscriptionUpdater(
        val poloniexMetaInformation: PoloniexMetaInformation,
        val session: WebSocketSession
) : Runnable {

    private val LOG = LoggerFactory.getLogger(PoloniexSubscriptionUpdater::class.java)

    override fun run() {
        if (session.isOpen) {
            LOG.info("Poloniex meta info update is started")
            updateExchangeSubscription()
        }
    }

    fun updateExchangeSubscription() {
        val newChannels = updatePoloniexMetaData()
        subscribeNewChannels(newChannels)
    }

    private fun subscribeNewChannels(newChannelsIds: Collection<Int>) {
        newChannelsIds.forEach { channelId -> session.subscribeChannel(channelId) }
    }

    /**
     * Updates poloniex meta information. Returns new channels ids to subscribe.
     */
    private fun updatePoloniexMetaData(): Collection<Int> {

        val poloniex = ExchangeFactory.INSTANCE.createExchange(PoloniexExchange::class.java.name) as PoloniexExchange
        val channelIdForTokensPair = poloniex.getTokensPairsWithChannelIds()

        val newChannelIds = channelIdForTokensPair.keys
                .filter { channelId -> !poloniexMetaInformation.channelIdForTokensPairs.containsKey(channelId) }
                .toList()

        poloniexMetaInformation.channelIdForTokensPairs = channelIdForTokensPair

        return newChannelIds
    }
}