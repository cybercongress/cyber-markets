package fund.cyber.markets.connectors.common.ws

import fund.cyber.markets.connectors.common.*
import fund.cyber.markets.connectors.helpers.concurrent
import fund.cyber.markets.helpers.retryUntilSuccess
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.delay
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit.MINUTES

interface WsExchange : Exchange {
    val wsAddress: String
}

abstract class WsCommonExchange(
        override val type: ExchangeType
) : WsExchange {
    private val LOGGER = LoggerFactory.getLogger(WsCommonExchange::class.java)!!

    private val channel = Channel<ExchangeMessage>()
    abstract val connector: WsConnector
    abstract val messageParser: ExchangeMessageParser
    abstract val reconnectable: Boolean


    protected abstract suspend fun updatePairs(): List<String>
    protected abstract suspend fun initMetadata()
    protected abstract fun getSubscribeMessage(pairSymbol: String): String
    protected abstract fun getPairsToSubscribe(): Collection<String>

    protected fun subscribePairs(pairs: Collection<String>) {
        pairs.forEach {
            connector.subscribeChannel(getSubscribeMessage(it))
        }
    }

    override fun subscribeData(): Channel<ExchangeMessage> {
        concurrent {
            retryUntilSuccess { initMetadata() }
            concurrent {
                while (true) {
                    delay(5, TimeUnit.MINUTES)
                    LOGGER.debug("Updating pairs for $name")
                    val pairs = updatePairs()
                    subscribePairs(pairs)
                    LOGGER.debug("Pairs for $name has been updated. ${pairs.size} new pairs found!")
                }
            }

            connector.connect{
                concurrent {
                    val result = messageParser.parseMessage(it)
                    getMessageHandler(type).invoke(result)
                }
            }
            subscribePairs(getPairsToSubscribe())

            if (reconnectable) {
                connector.reconnectEvery(5, MINUTES)
            }
        }

        return channel
    }

    private fun getMessageHandler(exchangeType: ExchangeType): suspend (List<ExchangeMessage>) -> Unit {
        return { messages ->
            messages.forEach { m ->
                when (exchangeType) {
                    ExchangeType.ORDERS ->
                        when (m) {
                            is OrdersUpdatesMessage -> if (!m.orders.isEmpty()) channel.send(m)
                            else -> handleUnknownMessage(m)
                        }

                    ExchangeType.TRADES ->
                        when (m) {
                            is TradesUpdatesMessage -> if (!m.trades.isEmpty()) channel.send(m)
                            else -> handleUnknownMessage(m)
                        }
                }
            }
        }
    }

    protected open fun handleUnknownMessage(message: ExchangeMessage) {

    }
}