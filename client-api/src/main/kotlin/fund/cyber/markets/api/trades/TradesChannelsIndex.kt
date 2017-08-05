package fund.cyber.markets.api.trades

import fund.cyber.markets.model.TokensPair
import kotlinx.coroutines.experimental.channels.Channel


class TradesChannelsIndex {


    fun channelFor(exchange: String, pair: TokensPair): TradesChannel {
        return Channel()
    }
}