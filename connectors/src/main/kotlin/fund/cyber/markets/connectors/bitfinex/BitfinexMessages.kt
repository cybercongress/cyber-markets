package fund.cyber.markets.connectors.bitfinex

import fund.cyber.markets.connectors.common.ExchangeMessage
import fund.cyber.markets.model.TokensPair

val channel_trades = "trades"
val channel_orders = "book"

class ChannelSubscribed(
        val channelId: Int,
        val tokensPair: TokensPair
) : ExchangeMessage()