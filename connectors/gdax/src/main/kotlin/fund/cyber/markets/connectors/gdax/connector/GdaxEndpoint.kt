package fund.cyber.markets.connectors.gdax.connector

import fund.cyber.markets.connectors.common.ExchangeMessage
import fund.cyber.markets.connectors.common.GDAX_WS_ENDPOINT
import fund.cyber.markets.connectors.common.ws.OrdersWsEndpoint
import fund.cyber.markets.connectors.common.ws.TradesWsEndpoint
import fund.cyber.markets.connectors.common.ws.pusher.PusherMessage
import org.slf4j.LoggerFactory

class GdaxTradesEndpoint : TradesWsEndpoint(GDAX_WS_ENDPOINT) {
    private val LOGGER = LoggerFactory.getLogger(GdaxTradesEndpoint::class.java)!!

    override val name: String = "GDAX Trades"
    override val messageParser = GdaxTradesMessageParser(channelSymbolForTokensPairs)
    override val pairsProvider = GdaxPairsProvider()

    override fun getSubscriptionMsgByChannelSymbol(pairSymbol: String): String {
        return """{"type":"subscribe","channels":[{"name":"matches","product_ids":["$pairSymbol"]}]}"""
    }

    override fun handleUnknownMessage(message: ExchangeMessage) {
        if (message is PusherMessage) {
            LOGGER.debug(message.message(name))
        } else {
            super.handleUnknownMessage(message)
        }
    }
}

class GdaxOrdersEndpoint : OrdersWsEndpoint(GDAX_WS_ENDPOINT) {
    private val LOGGER = LoggerFactory.getLogger(GdaxOrdersEndpoint::class.java)!!

    override val name: String = "GDAX Orders"
    override val messageParser = GdaxOrdersMessageParser(channelSymbolForTokensPairs)
    override val pairsProvider = GdaxPairsProvider()

    override fun getSubscriptionMsgByChannelSymbol(pairSymbol: String): String {
        return """{"type":"subscribe","channels":[{"name":"level2_50","product_ids":["$pairSymbol"]}]}"""
    }

    override fun handleUnknownMessage(message: ExchangeMessage) {
        if (message is PusherMessage) {
            LOGGER.debug(message.message(name))
        } else {
            super.handleUnknownMessage(message)
        }
    }
}
