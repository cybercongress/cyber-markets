package fund.cyber.markets.connectors.common

import kotlinx.coroutines.experimental.channels.Channel


interface Exchange {
    fun subscribeData(): Channel<ExchangeMessage>
    val name: String
    val type: ExchangeType
}




