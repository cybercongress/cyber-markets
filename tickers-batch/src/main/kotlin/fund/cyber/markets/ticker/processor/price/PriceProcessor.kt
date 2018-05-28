package fund.cyber.markets.ticker.processor.price

import fund.cyber.markets.common.model.TokenPrice
import fund.cyber.markets.common.model.TokenTicker

interface PriceProcessor {
    val priceName: String

    fun calculate(tickers: List<TokenTicker>): List<TokenPrice>?
}