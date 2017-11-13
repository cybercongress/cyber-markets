package fund.cyber.markets.tickers.model

import fund.cyber.markets.dto.TokensPair
import fund.cyber.markets.model.Trade
import java.math.BigDecimal

class WindowStats {

    var exchange: String? = null
    var tokensPair: TokensPair? = null
    var baseAmount: BigDecimal = BigDecimal.ZERO
    var quoteAmount: BigDecimal = BigDecimal.ZERO
    var price: BigDecimal = BigDecimal.ZERO
    var minPrice: BigDecimal? = null
    var maxPrice: BigDecimal? = null

    fun add(trade: Trade): WindowStats {

        if (trade.baseAmount == null || trade.quoteAmount == null || trade.pair == null)
            return this

        if (this.exchange == null) {
            this.exchange = trade.exchange
        }

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
