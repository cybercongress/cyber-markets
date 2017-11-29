package fund.cyber.markets.tickers.model

import fund.cyber.markets.dto.TokensPair
import fund.cyber.markets.model.Trade
import java.math.BigDecimal

data class Ticker(
    var exchange: String?,
    var tokensPair: TokensPair?,
    var windowDuration: Long,
    var baseAmount: BigDecimal,
    var quoteAmount: BigDecimal,
    var price: BigDecimal,
    var minPrice: BigDecimal?,
    var maxPrice: BigDecimal?,
    var tradeCount: Long
) {

    constructor(windowDuration: Long) : this(null, null, windowDuration, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, null, null, 0)

    fun add(trade: Trade): Ticker {

        if (!validTrade(trade)) {
            return this
        }
        if (exchange == null) {
            exchange = trade.exchange
        }
        if (tokensPair == null) {
            tokensPair = trade.pair
        }

        quoteAmount = quoteAmount.plus(trade.quoteAmount)
        baseAmount = baseAmount.plus(trade.baseAmount)

        minPrice =
                if (minPrice == null)
                    trade.quoteAmount.div(trade.baseAmount)
                else
                    minPrice?.min(trade.quoteAmount.div(trade.baseAmount))

        maxPrice =
                if (maxPrice == null)
                    trade.quoteAmount.div(trade.baseAmount)
                else
                    maxPrice?.max(trade.quoteAmount.div(trade.baseAmount))

        tradeCount++

        return this
    }

    fun add(ticker: Ticker): Ticker {

        quoteAmount = quoteAmount.plus(ticker.quoteAmount)
        baseAmount = baseAmount.plus(ticker.baseAmount)

        if (tokensPair == null) {
            tokensPair = ticker.tokensPair
        }
        if (exchange == null) {
            exchange = ticker.exchange
        }

        minPrice =
                if (minPrice == null)
                    ticker.minPrice
                else
                    this.minPrice?.min(ticker.minPrice)

        maxPrice =
                if (maxPrice == null)
                    ticker.maxPrice
                else
                    this.maxPrice?.max(ticker.maxPrice)

        tradeCount += ticker.tradeCount

        return this
    }

    fun calcPrice(): Ticker {
        if (!(quoteAmount.compareTo(BigDecimal.ZERO) == 0 || baseAmount.compareTo(BigDecimal.ZERO) == 0)) {
            price = quoteAmount.div(baseAmount)
        }

        return this
    }

    fun setExchangeString(exchange: String) : Ticker {
        this.exchange = exchange

        return this
    }

    private fun validTrade(trade: Trade): Boolean {
        return !(trade.baseAmount == null
                || trade.quoteAmount == null
                || trade.pair == null
                || trade.quoteAmount.compareTo(BigDecimal.ZERO) == 0
                || trade.baseAmount.compareTo(BigDecimal.ZERO) == 0)
    }

}
