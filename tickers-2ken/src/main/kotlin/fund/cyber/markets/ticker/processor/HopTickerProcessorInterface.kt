package fund.cyber.markets.ticker.processor

import fund.cyber.markets.model.Ticker
import fund.cyber.markets.model.TokensPair

interface HopTickerProcessorInterface {

    fun poll()

    fun update(currentMillisHop: Long)

    fun get(pair: TokensPair, exchange: String, currentMillisHop: Long): Ticker

}