package fund.cyber.markets.connectors.bitfinex.connector

import fund.cyber.markets.connectors.common.ExchangeMessage
import fund.cyber.markets.model.TokensPairInitializer

val channel_trades = "trades"
val channel_orders = "book"

class ChannelSubscribed(
        val channelId: Int,
        val tokensPairInitializer: TokensPairInitializer
) : ExchangeMessage()