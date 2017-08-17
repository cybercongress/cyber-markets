package fund.cyber.markets.connectors.common

import kotlinx.coroutines.experimental.channels.Channel


interface Exchange {
    fun subscribeData(): Channel<TradesAndOrdersUpdatesMessage>
    val name: String
}




