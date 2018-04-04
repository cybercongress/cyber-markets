package fund.cyber.markets.connector

interface ExchangeConnector {
    fun connect()
    fun disconnect()
    fun reconnect() {
        disconnect()
        connect()
    }
    fun isAlive(): Boolean
    fun subscribeTrades()
    fun subscribeAll() {
        subscribeTrades()
    }
    fun updateTokensPairs()
}