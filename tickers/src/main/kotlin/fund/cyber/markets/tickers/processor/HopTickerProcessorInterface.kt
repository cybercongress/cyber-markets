package fund.cyber.markets.tickers.processor

import fund.cyber.markets.dto.TokensPair
import fund.cyber.markets.model.Ticker

interface HopTickerProcessorInterface {

    fun poll()

    fun update(currentMillisHop: Long)

    fun get(pair: TokensPair, exchange: String, currentMillisHop: Long): Ticker

}