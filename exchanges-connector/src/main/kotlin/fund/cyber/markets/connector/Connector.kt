package fund.cyber.markets.connector

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
}