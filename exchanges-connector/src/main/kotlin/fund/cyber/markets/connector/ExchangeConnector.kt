package fund.cyber.markets.connector

interface ExchangeConnector {
    fun start() {
        connect()
        subscribeAll()
    }
    fun connect()
    fun disconnect()
    fun restart() {
        disconnect()
        start()
    }
    fun isAlive(): Boolean
    fun subscribeTrades()
    fun subscribeAll() {
        subscribeTrades()
    }
    fun updateTokensPairs()
}