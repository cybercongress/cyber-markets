package fund.cyber.markets.exchanges.bitfinex

import fund.cyber.markets.model.TokensPair
import fund.cyber.markets.webscoket.ExchangeMessage
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession


val channel_trades = "trades"
val channel_orders = "book"

fun WebSocketSession.subscribeTradeChannel(channelSymbol: String) {
    sendMessage(TextMessage("""{"event":"subscribe","channel":"$channel_trades","symbol":"$channelSymbol"}"""))
}

fun WebSocketSession.subscribeOrderChannel(channelSymbol: String) {
    val message = """{"event":"subscribe","channel":"$channel_orders","symbol":"$channelSymbol",prec: "R0",len: 100}"""
    sendMessage(TextMessage(message))
}

class TradeChannelSubscribed(
        val channelId: Int,
        val tokensPair: TokensPair
) : ExchangeMessage()