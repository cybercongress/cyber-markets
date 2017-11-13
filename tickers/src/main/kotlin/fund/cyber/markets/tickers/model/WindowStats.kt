package fund.cyber.markets.tickers.model

import fund.cyber.markets.dto.TokensPair
import fund.cyber.markets.model.Trade
import java.math.BigDecimal

class WindowStats {

    var tokensPair: TokensPair?
    var baseAmount: BigDecimal
    var quoteAmount: BigDecimal
    var price: BigDecimal
    var minPrice: BigDecimal?
    var maxPrice: BigDecimal?

    init {
        tokensPair = null
        baseAmount = BigDecimal.ZERO
        quoteAmount = BigDecimal.ZERO
        price = BigDecimal.ZERO
        minPrice = null
        maxPrice = null
    }

    fun add(trade: Trade): WindowStats {

        if (trade.baseAmount == null || trade.quoteAmount == null || trade.pair == null)
            return this

        if (this.tokensPair == null) {
            this.tokensPair = trade.pair
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

        return this
    }

    fun calcPrice(): WindowStats {
        if (quoteAmount != BigDecimal.ZERO && baseAmount != BigDecimal.ZERO) {
            price = quoteAmount.div(baseAmount)
        }

        return this
    }

}
