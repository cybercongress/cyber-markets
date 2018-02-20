package fund.cyber.markets.model

enum class BaseTokens {
    BTC, ETH, USD, USDT;

    fun symbols(): List<String> {
        return BaseTokens.values().map { it.name }
    }
}