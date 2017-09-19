package fund.cyber.markets.model

import java.math.BigDecimal

/**
 * @author mgergalov
 */
data class Trade(
        val tradeId: String,
        val exchange: String,
        val timestamp: Long,
        val type: TradeType,
        val baseToken: String,
        val quoteToken: String,
        val baseAmount: BigDecimal,
        val quoteAmount: BigDecimal,
        val spotPrice: BigDecimal,
        val reverted: Boolean
) {

    companion object {
        fun of(tradeId: String,
               exchange: String,
               timestamp: Long,
               type: TradeType,
               baseAmount: BigDecimal,
               quoteAmount: BigDecimal,
               spotPrice: BigDecimal,
               tokensPair: TokensPair
        ): Trade {
            val resType: TradeType
            val resBaseAmount: BigDecimal
            val resQuoteAmount: BigDecimal
            val resSpotPrice: BigDecimal
            if (tokensPair.reverted) {
                resType = revertType(type)
                resBaseAmount = quoteAmount
                resQuoteAmount = baseAmount
                resSpotPrice = quoteAmount / baseAmount
            } else {
                resType = type
                resBaseAmount = baseAmount
                resQuoteAmount = quoteAmount
                resSpotPrice = spotPrice
            }
            return Trade(tradeId, exchange, timestamp, type, tokensPair.base, tokensPair.quote, resBaseAmount,
                    resQuoteAmount, resSpotPrice, tokensPair.reverted)
        }

        private fun revertType(type: TradeType): TradeType {
            return when (type) {
                TradeType.BUY -> TradeType.SELL
                TradeType.SELL -> TradeType.BUY
                else -> type
            }
        }
    }

}