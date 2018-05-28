package fund.cyber.markets.ticker.processor.price

import fund.cyber.markets.common.model.TokenPrice
import fund.cyber.markets.common.model.TokenTicker

class VwapPriceProcessor(
    override val priceName: String = "VWAP"
) : PriceProcessor {

    override fun calculate(tickers: List<TokenTicker>): List<TokenPrice>? {
        return null
    }

}