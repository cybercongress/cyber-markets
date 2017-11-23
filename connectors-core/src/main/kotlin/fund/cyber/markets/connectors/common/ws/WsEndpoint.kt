package fund.cyber.markets.connectors.common.ws

import fund.cyber.markets.connectors.common.*
import fund.cyber.markets.connectors.helpers.concurrent
import fund.cyber.markets.helpers.retryUntilSuccess
import fund.cyber.markets.model.TokensPairInitializer
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.delay
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit


interface WsEndpoint<M: ExchangeMessage>: Endpoint<M> {
    val wsAddress: String
}

abstract class ExchangeWsEndpoint<M : ExchangeMessage>(
        override val wsAddress: String
) : WsEndpoint<M> {
    private val LOGGER = LoggerFactory.getLogger(ExchangeWsEndpoint::class.java)!!

    protected val channel = Channel<M>()
    protected val channelSymbolForTokensPairs = hashMapOf<String, TokensPairInitializer>()

    protected abstract val connector: WsConnector
    protected abstract val messageParser: ExchangeMessageParser
    protected abstract val messageHandler: (String) -> Unit
    protected abstract val pairsProvider: PairsProvider

    protected abstract fun getSubscriptionMsgByChannelSymbol(pairSymbol: String): String

    override fun subscribe(): Channel<M> {
        concurrent {
            retryUntilSuccess { channelSymbolForTokensPairs.putAll(pairsProvider.getPairs()) }

            connector.connect(messageHandler) {
                channelSymbolForTokensPairs.keys.forEach { pairSymbol ->
                    connector.sendMessage(getSubscriptionMsgByChannelSymbol(pairSymbol))
                }
            }

            runPairsUpdate()
        }
        return channel
    }

    private fun runPairsUpdate() {
        concurrent {
            while (true) {
                delay(5, TimeUnit.MINUTES)
                LOGGER.debug("Updating pairs for $name")
                val pairs = pairsProvider.getPairs()
                pairs.forEach { p ->
                    val result = channelSymbolForTokensPairs.put(p.key, p.value)
                    result ?: connector.sendMessage(getSubscriptionMsgByChannelSymbol(p.key))

                }
                LOGGER.debug("Pairs for $name has been updated. ${pairs.size} new pairs found!")
            }
        }
    }
}

abstract class TradesWsEndpoint(wsAddress: String): ExchangeWsEndpoint<TradesUpdatesMessage>(wsAddress) {
    override val connector: WsConnector = ReconnectableWsConnector(wsAddress)

    override val messageHandler: (String) -> Unit = { message ->
        val result = messageParser.parseMessage(message)
        when (result) {
            is TradesUpdatesMessage -> if (result.trades.isNotEmpty()) concurrent { channel.send(result) }
            else -> handleUnknownMessage(result)
        }
    }

    protected open fun handleUnknownMessage(message: ExchangeMessage) {}
}

abstract class OrdersWsEndpoint(wsAddress: String) : ExchangeWsEndpoint<OrdersUpdatesMessage>(wsAddress) {
    override val connector: WsConnector = ReconnectableWsConnector(
            wsAddress,
            TimeUnit.MILLISECONDS.convert(5L, TimeUnit.MINUTES)
    )

    override val messageHandler: (String) -> Unit = { message ->
        val result = messageParser.parseMessage(message)
        when (result) {
            is OrdersUpdatesMessage -> if (result.orders.isNotEmpty()) concurrent { channel.send(result) }
            else -> handleUnknownMessage(result)
        }
    }

    protected open fun handleUnknownMessage(message: ExchangeMessage) {}
}