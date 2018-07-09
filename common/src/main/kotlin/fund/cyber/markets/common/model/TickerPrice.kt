package fund.cyber.markets.common.model

import java.math.BigDecimal

data class TickerPrice(
    var open: BigDecimal,
    var close: BigDecimal,
    var min: BigDecimal,
    var max: BigDecimal
) {
    constructor(initialPrice: BigDecimal): this(
        open = initialPrice,
        close = initialPrice,
        min = initialPrice,
        max = initialPrice
    )

    fun update(value: BigDecimal): TickerPrice {
        close = value

        if (value < min) {
            min = value
        }

        if (value > max) {
            max = value
        }

        return this
    }

    fun update(price: TickerPrice): TickerPrice {
        update(price.min)
        update(price.max)
        update(price.close)

        return this
    }

}