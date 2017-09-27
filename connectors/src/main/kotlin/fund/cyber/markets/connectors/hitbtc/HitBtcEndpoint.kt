package fund.cyber.markets.connectors.hitbtc

import fund.cyber.markets.connectors.common.HITBTC_WS_ENDPOINT
import fund.cyber.markets.connectors.common.ws.OrdersWsEndpoint
import fund.cyber.markets.connectors.common.ws.TradesWsEndpoint
import fund.cyber.markets.model.TokensPairInitializer
import java.math.BigDecimal

class HitBtcTokensPairInitializer(
        val symbol: String,
        val lotSize: BigDecimal,
        val priceStep: BigDecimal,
        base: String,
        quote: String
) : TokensPairInitializer(base, quote)

class HitBtcTradesEndpoint: TradesWsEndpoint(HITBTC_WS_ENDPOINT) {

    @Suppress("UNCHECKED_CAST")
    val channelSymbolForTokensPairsHitBtc = channelSymbolForTokensPairs as HashMap<String, HitBtcTokensPairInitializer>

    override val name: String = "HitBtc Trades"
    override val messageParser = HitBtcTradesMessageParser(channelSymbolForTokensPairsHitBtc)
    override val pairsProvider = HitBtcPairsProvider()

    override fun getSubscriptionMsgByChannelSymbol(pairSymbol: String): String = ""
}

class HitBtcOrdersEndpoint: OrdersWsEndpoint(HITBTC_WS_ENDPOINT) {

    @Suppress("UNCHECKED_CAST")
    val channelSymbolForTokensPairsHitBtc = channelSymbolForTokensPairs as HashMap<String, HitBtcTokensPairInitializer>

    override val name: String = "HitBtc Orders"
    override val messageParser = HitBtcOrdersMessageParser(channelSymbolForTokensPairsHitBtc)
    override val pairsProvider = HitBtcPairsProvider()

    override fun getSubscriptionMsgByChannelSymbol(pairSymbol: String): String = ""
}
