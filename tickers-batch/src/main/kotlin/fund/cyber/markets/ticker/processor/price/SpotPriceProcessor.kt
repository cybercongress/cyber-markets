package fund.cyber.markets.ticker.processor.price

import fund.cyber.markets.common.model.TokenPrice
import fund.cyber.markets.common.model.TokenTicker

class SpotPriceProcessor(
    override val methodName: String = "SpotPrice"
) : PriceProcessor {

    override fun calculate(tickers: List<TokenTicker>): List<TokenPrice> {
        return mutableListOf()
    }

}