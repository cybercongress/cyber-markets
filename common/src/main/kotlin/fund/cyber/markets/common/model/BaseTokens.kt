package fund.cyber.markets.common.model

enum class BaseTokens {
    BTC, ETH, USD;

    fun symbols(): List<String> {
        return BaseTokens.values().map { it.name }
    }
}