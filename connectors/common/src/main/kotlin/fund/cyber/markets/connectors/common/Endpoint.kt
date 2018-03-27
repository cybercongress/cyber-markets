package fund.cyber.markets.connectors.common

import kotlinx.coroutines.experimental.channels.Channel

interface Endpoint<M: ExchangeMessage> {
    fun subscribe(): Channel<M>
    val name: String
}
