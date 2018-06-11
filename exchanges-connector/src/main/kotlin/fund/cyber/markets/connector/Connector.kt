package fund.cyber.markets.connector

import fund.cyber.markets.common.model.Token
import fund.cyber.markets.common.model.TokensPair

interface Connector {
    fun start() {
        connect()
        subscribe()
    }

    fun restart() {
        disconnect()
        start()
    }

    fun subscribe()
    fun connect()
    fun disconnect()
    fun isAlive(): Boolean
    fun updateTokensPairs()
    fun getTokensPairs(): Set<TokensPair>
    fun getTokens(): Set<Token>
}