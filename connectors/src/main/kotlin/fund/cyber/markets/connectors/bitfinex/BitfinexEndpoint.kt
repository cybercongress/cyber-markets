package fund.cyber.markets.connectors.bitfinex

import fund.cyber.markets.connectors.common.BITFINEX_WS_ENDPOINT
import fund.cyber.markets.connectors.common.ExchangeMessage
import fund.cyber.markets.connectors.common.PairsProvider
import fund.cyber.markets.connectors.common.ws.ExchangeMessageParser
import fund.cyber.markets.connectors.common.ws.OrdersWsEndpoint
import fund.cyber.markets.connectors.common.ws.TradesWsEndpoint
import fund.cyber.markets.model.TokensPair
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap


class BitfinexTradesEndpoint: TradesWsEndpoint(BITFINEX_WS_ENDPOINT) {
    private val LOGGER = LoggerFactory.getLogger(BitfinexTradesEndpoint::class.java)!!

    private val tradesChannelIdForTokensPair = ConcurrentHashMap<Int, TokensPair>(64, 0.75f, 5)

    override val name: String = "Bitfinex Trades"
    override val messageParser = BitfinexTradesMessageParser(channelSymbolForTokensPairs, tradesChannelIdForTokensPair)
    override val pairsProvider: PairsProvider = BitfinexPairsProvider()

    override fun getSubscriptionMsgByChannelSymbol(pairSymbol: String): String {
        return """{"event":"subscribe","channel":"trades","pair":"$pairSymbol"}"""
    }

    override fun handleUnknownMessage(message: ExchangeMessage) {
        super.handleUnknownMessage(message)
        if (message is ChannelSubscribed) {
            tradesChannelIdForTokensPair.put(message.channelId, message.tokensPair)
            LOGGER.debug("Subscribed to Bitfinex ${message.tokensPair.label()} channel")
        }
    }
}


class BitfinexOrdersEndpoint: OrdersWsEndpoint(BITFINEX_WS_ENDPOINT) {
    private val LOGGER = LoggerFactory.getLogger(BitfinexOrdersEndpoint::class.java)!!

    private val ordersChannelIdForTokensPair = ConcurrentHashMap<Int, TokensPair>(64, 0.75f, 5)

    override val name: String = "Bitfinex Orders"
    override val messageParser = BitfinexOrdersMessageParser(channelSymbolForTokensPairs, ordersChannelIdForTokensPair)
    override val pairsProvider: PairsProvider = BitfinexPairsProvider()

    override fun getSubscriptionMsgByChannelSymbol(pairSymbol: String): String {
        return """{"event":"subscribe","channel":"book","pair":"$pairSymbol", "freq":"F0"}"""
    }

    override fun handleUnknownMessage(message: ExchangeMessage) {
        if (message is ChannelSubscribed) {
            ordersChannelIdForTokensPair.put(message.channelId, message.tokensPair)
            LOGGER.debug("Subscribed to Bitfinex ${message.tokensPair.label()} channel")
        } else {
            super.handleUnknownMessage(message)
        }
    }
}
