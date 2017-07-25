package fund.cyber.markets.exchanges.bitfinex

import fund.cyber.markets.model.TokensPair
import fund.cyber.markets.webscoket.ExchangeMessage

val channel_trades = "trades"
val channel_orders = "book"

class TradeChannelSubscribed(
        val channelId: Int,
        val tokensPair: TokensPair
) : ExchangeMessage()