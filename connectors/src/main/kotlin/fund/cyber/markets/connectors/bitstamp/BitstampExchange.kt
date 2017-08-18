package fund.cyber.markets.connectors.bitstamp

import fund.cyber.markets.connectors.common.ExchangeMessage
import fund.cyber.markets.connectors.common.ExchangeType
import fund.cyber.markets.connectors.common.ws.ReconnectableWsConnector
import fund.cyber.markets.connectors.common.ws.WsCommonExchange
import fund.cyber.markets.connectors.common.ws.WsConnector
import fund.cyber.markets.connectors.common.ws.pusher.PusherMessage
import fund.cyber.markets.model.TokensPair
import org.slf4j.LoggerFactory

class BitstampExchange(type: ExchangeType): WsCommonExchange(type) {
    private val LOGGER = LoggerFactory.getLogger(BitstampExchange::class.java)!!

    private val tradeChannelPrefix = "live_trades_"
    private val fullOrderBookChannelPrefix = "diff_order_book"
    override val name = "Bitstamp"
    override val wsAddress = "wss://ws.pusherapp.com:443/app/de504dc5763aeef9ff52?protocol=6"
    override val connector: WsConnector = ReconnectableWsConnector(wsAddress, name)
    override val reconnectable: Boolean
        get() = type == ExchangeType.ORDERS

    private val channelSymbolsForTokensPairs = hashMapOf<String, TokensPair>()
    override val messageParser = BitstampMessageParser(channelSymbolsForTokensPairs)


    override suspend fun updatePairs(): List<String> = emptyList()

    // Pairs available for api. See https://www.bitstamp.net/websocket/
    override suspend fun initMetadata() {
        channelSymbolsForTokensPairs.put("btceur", TokensPair("BTC", "EUR"))
        channelSymbolsForTokensPairs.put("eurusd", TokensPair("EUR", "USD"))
        channelSymbolsForTokensPairs.put("xrpusd", TokensPair("XRP", "USD"))
        channelSymbolsForTokensPairs.put("xrpeur", TokensPair("XRP", "EUR"))
        channelSymbolsForTokensPairs.put("xrpbtc", TokensPair("XRP", "BTC"))
        channelSymbolsForTokensPairs.put("ltcusd", TokensPair("LTC", "USD"))
        channelSymbolsForTokensPairs.put("ltceur", TokensPair("LTC", "EUR"))
        channelSymbolsForTokensPairs.put("ltcbtc", TokensPair("LTC", "BTC"))
    }

    override fun getPairsToSubscribe(): Collection<String> {
        return channelSymbolsForTokensPairs.keys
    }

    override fun getSubscribeMessage(pairSymbol: String): String {
        return when(type) {
            ExchangeType.ORDERS -> """{"event":"pusher:subscribe","data":{"channel":"$fullOrderBookChannelPrefix$pairSymbol"}}"""
            ExchangeType.TRADES -> """{"event":"pusher:subscribe","data":{"channel":"$tradeChannelPrefix$pairSymbol"}}"""
        }
    }

    override fun handleUnknownMessage(message: ExchangeMessage) {
        if (message is PusherMessage) {
            LOGGER.debug(message.message(name))
        }
    }
}
