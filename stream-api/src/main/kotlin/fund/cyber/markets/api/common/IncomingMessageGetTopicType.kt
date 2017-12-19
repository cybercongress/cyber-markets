package fund.cyber.markets.api.common

/**
 * @author mgergalov
 */
enum class IncomingMessageGetTopicType {
    PAIRS, PAIRS_BY_TOKEN, EXCHANGES
}

enum class IncomingMessageSubscribeTopicType {
    TRADES, ORDERS, TICKERS
}