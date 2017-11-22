package fund.cyber.markets.connectors.poloniex.connector

import fund.cyber.markets.connectors.common.POLONIEX_WS_ENDPOINT
import fund.cyber.markets.connectors.common.ws.OrdersWsEndpoint
import fund.cyber.markets.connectors.common.ws.TradesWsEndpoint
import fund.cyber.markets.connectors.poloniex.connector.PoloniexOrdersMessageParser
import fund.cyber.markets.connectors.poloniex.connector.PoloniexPairsProvider
import fund.cyber.markets.connectors.poloniex.connector.PoloniexTradesMessageParser


class PoloniexOrdersEndpoint: OrdersWsEndpoint(POLONIEX_WS_ENDPOINT) {
    override val name: String = "Poloniex Orders"
    override val messageParser = PoloniexOrdersMessageParser(channelSymbolForTokensPairs)
    override val pairsProvider = PoloniexPairsProvider()

    override fun getSubscriptionMsgByChannelSymbol(pairSymbol: String): String {
        return """{"command":"subscribe","channel":"$pairSymbol"}"""
    }

}

class PoloniexTradesEndpoint: TradesWsEndpoint(POLONIEX_WS_ENDPOINT) {
    override val name: String = "Poloniex Trades"
    override val messageParser = PoloniexTradesMessageParser(channelSymbolForTokensPairs)
    override val pairsProvider = PoloniexPairsProvider()

    override fun getSubscriptionMsgByChannelSymbol(pairSymbol: String): String {
        return """{"command":"subscribe","channel":"$pairSymbol"}"""
    }

}
