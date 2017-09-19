package fund.cyber.markets.connectors.bitstamp

import fund.cyber.markets.connectors.bitfinex.BitfinexOrdersEndpoint
import fund.cyber.markets.connectors.common.BITSTAMP_WS_ENDPOINT
import fund.cyber.markets.connectors.common.ExchangeMessage
import fund.cyber.markets.connectors.common.ws.OrdersWsEndpoint
import fund.cyber.markets.connectors.common.ws.TradesWsEndpoint
import fund.cyber.markets.connectors.common.ws.pusher.PusherMessage
import org.slf4j.LoggerFactory

private val FULL_ORDER_BOOK_CHANNEL_PREFIX = "diff_order_book_"
private val TRADES_CHANNEL_PREFIX = "live_trades_"

class BitstampTradesEndpoint: TradesWsEndpoint(BITSTAMP_WS_ENDPOINT) {
    private val LOGGER = LoggerFactory.getLogger(BitfinexOrdersEndpoint::class.java)!!

    override val name: String = "Bitstamp Trades"
    override val messageParser = BitstampTradesMessageParser(channelSymbolForTokensPairs)
    override val pairsProvider = BitstampPairsProvider()

    override fun getSubscriptionMsgByChannelSymbol(pairSymbol: String): String {
        return """{"event":"pusher:subscribe","data":{"channel":"$TRADES_CHANNEL_PREFIX$pairSymbol"}}"""
    }

    override fun handleUnknownMessage(message: ExchangeMessage) {
        if (message is PusherMessage) {
            LOGGER.debug(message.message(name))
        } else {
            super.handleUnknownMessage(message)
        }
    }
}

class BitstampOrdersEndpoint: OrdersWsEndpoint(BITSTAMP_WS_ENDPOINT) {
    private val LOGGER = LoggerFactory.getLogger(BitfinexOrdersEndpoint::class.java)!!

    override val name: String = "Bitstamp Orders"
    override val messageParser = BitstampOrdersMessageParser(channelSymbolForTokensPairs)
    override val pairsProvider = BitstampPairsProvider()

    override fun getSubscriptionMsgByChannelSymbol(pairSymbol: String): String {
        return """{"event":"pusher:subscribe","data":{"channel":"$FULL_ORDER_BOOK_CHANNEL_PREFIX$pairSymbol"}}"""
    }

    override fun handleUnknownMessage(message: ExchangeMessage) {
        if (message is PusherMessage) {
            LOGGER.debug(message.message(name))
        } else {
            super.handleUnknownMessage(message)
        }
    }
}
