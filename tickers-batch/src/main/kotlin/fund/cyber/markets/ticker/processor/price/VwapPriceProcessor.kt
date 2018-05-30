package fund.cyber.markets.ticker.processor.price

import fund.cyber.markets.common.model.TokenPrice
import fund.cyber.markets.common.model.TokenTicker
import org.springframework.stereotype.Component

@Component
class VwapPriceProcessor(
    override val methodName: String = "VWAP"
) : PriceProcessor {

    override fun calculate(tickers: List<TokenTicker>): List<TokenPrice> {
        return mutableListOf()
    }

}