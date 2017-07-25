package fund.cyber.markets.exchanges

import fund.cyber.markets.webscoket.TradesAndOrdersUpdatesMessage
import kotlinx.coroutines.experimental.channels.Channel


interface Exchange {
    fun subscribeData(): Channel<TradesAndOrdersUpdatesMessage>
    val name: String
}




