package fund.cyber.markets.connectors.bitstamp

import fund.cyber.markets.connectors.common.BITSTAMP_WS_ENDPOINT
import fund.cyber.markets.connectors.common.PairsProvider
import fund.cyber.markets.connectors.common.ws.ExchangeMessageParser
import fund.cyber.markets.connectors.common.ws.OrdersWsEndpoint
import fund.cyber.markets.connectors.common.ws.TradesWsEndpoint

private val FULL_ORDER_BOOK_CHANNEL_PREFIX = "diff_order_book_"
private val TRADES_CHANNEL_PREFIX = "live_trades_"

class BitstampTradesEndpoint: TradesWsEndpoint(BITSTAMP_WS_ENDPOINT) {
    override val name: String = "Bitstamp Trades"
    override val messageParser: ExchangeMessageParser = BitstampTradesMessageParser(channelSymbolForTokensPairs)
    override val pairsProvider: PairsProvider = BitstampPairsProvider()

    override fun getSubscriptionMsgByChannelSymbol(pairSymbol: String): String {
        return """{"event":"pusher:subscribe","data":{"channel":"$TRADES_CHANNEL_PREFIX$pairSymbol"}}"""
    }
}

class BitstampOrdersEndpoint: OrdersWsEndpoint(BITSTAMP_WS_ENDPOINT) {
    override val name: String = "Bitstamp Orders"
    override val messageParser: ExchangeMessageParser = BitstampOrdersMessageParser(channelSymbolForTokensPairs)
    override val pairsProvider: PairsProvider = BitstampPairsProvider()

    override fun getSubscriptionMsgByChannelSymbol(pairSymbol: String): String {
        return """{"event":"pusher:subscribe","data":{"channel":"$FULL_ORDER_BOOK_CHANNEL_PREFIX$pairSymbol"}}"""
    }
}
