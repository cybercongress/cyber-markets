package fund.cyber.markets.connectors.poloniex

import fund.cyber.markets.connectors.common.POLONIEX_WS_ENDPOINT
import fund.cyber.markets.connectors.common.PairsProvider
import fund.cyber.markets.connectors.common.ws.ExchangeMessageParser
import fund.cyber.markets.connectors.common.ws.OrdersWsEndpoint
import fund.cyber.markets.connectors.common.ws.TradesWsEndpoint
import fund.cyber.markets.model.TokensPair


class PoloniexOrdersEndpoint: OrdersWsEndpoint(POLONIEX_WS_ENDPOINT) {
    override val name: String = "Poloniex Orders"
    override val messageParser: ExchangeMessageParser = PoloniexOrdersMessageParser(channelSymbolForTokensPairs)
    override val pairsProvider: PairsProvider = PoloniexPairsProvider()

    override fun getSubscriptionMsgByChannelSymbol(pairSymbol: String): String {
        return """{"command":"subscribe","channel":"$pairSymbol"}"""
    }

}

class PoloniexTradesEndpoint: TradesWsEndpoint(POLONIEX_WS_ENDPOINT) {
    override val name: String = "Poloniex Trades"
    override val messageParser: ExchangeMessageParser = PoloniexTradesMessageParser(channelSymbolForTokensPairs)
    override val pairsProvider: PairsProvider = PoloniexPairsProvider()

    override fun getSubscriptionMsgByChannelSymbol(pairSymbol: String): String {
        return """{"command":"subscribe","channel":"$pairSymbol"}"""
    }

}
