package fund.cyber.markets.connector

interface ExchangeConnector {
    fun connect()
    fun disconnect()
    fun isAlive(): Boolean
    fun subscribeTrades()
    fun subscribeOrders()
    fun subscribeOrderBook()
    fun subscribeAll() {
        subscribeTrades()
        subscribeOrders()
        subscribeOrderBook()
    }
    fun updateTokensPairs()
}