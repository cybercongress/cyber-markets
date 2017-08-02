package fund.cyber.markets.connectors.bitstamp


import fund.cyber.markets.connectors.bitfinex.BitfinexExchange
import fund.cyber.markets.connectors.common.ExchangeMessage
import fund.cyber.markets.connectors.common.ws.ReconnectableWsExchange
import fund.cyber.markets.connectors.common.ws.pusher.PusherMessage
import fund.cyber.markets.model.TokensPair
import io.undertow.websockets.core.WebSocketChannel
import io.undertow.websockets.core.WebSockets
import org.slf4j.LoggerFactory

/**
 * Created by aalbov on 1.8.17.
 */
class BitstampExchange: ReconnectableWsExchange() {
    private val LOGGER = LoggerFactory.getLogger(BitfinexExchange::class.java)!!

    private val channelPrefix = "live_trades_"
    override val name = "Bitstamp"
    override val wsAddress = "wss://ws.pusherapp.com:443/app/de504dc5763aeef9ff52?protocol=6"

    private val channelSymbolsForTokensPairs = hashMapOf<String, TokensPair>()
    override val messageParser = BitstampMessageParser(channelSymbolsForTokensPairs)

    // Pairs available for api. See https://www.bitstamp.net/websocket/
    override suspend fun initializeMetadata() {
        channelSymbolsForTokensPairs.put("${channelPrefix}btceur", TokensPair("BTC", "EUR"))
        channelSymbolsForTokensPairs.put("${channelPrefix}eurusd", TokensPair("EUR", "USD"))
        channelSymbolsForTokensPairs.put("${channelPrefix}xrpusd", TokensPair("XRP", "USD"))
        channelSymbolsForTokensPairs.put("${channelPrefix}xrpeur", TokensPair("XRP", "EUR"))
        channelSymbolsForTokensPairs.put("${channelPrefix}xrpbtc", TokensPair("XRP", "BTC"))
        channelSymbolsForTokensPairs.put("${channelPrefix}ltcusd", TokensPair("LTC", "USD"))
        channelSymbolsForTokensPairs.put("${channelPrefix}ltceur", TokensPair("LTC", "EUR"))
        channelSymbolsForTokensPairs.put("${channelPrefix}ltcbtc", TokensPair("LTC", "BTC"))
    }

    override fun subscribeChannels(connection: WebSocketChannel) {
        channelSymbolsForTokensPairs.keys.forEach { channelId ->
            WebSockets.sendText("""{"event":"pusher:subscribe","data":{"channel":"$channelId"}}""", connection, null)
        }
    }

    override fun handleUnknownMessage(message: ExchangeMessage) {
        if (message is PusherMessage) {
            LOGGER.debug(message.message(name))
        }
    }
}
