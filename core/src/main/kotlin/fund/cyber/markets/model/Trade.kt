package fund.cyber.markets.model

import java.math.BigDecimal
import java.time.Instant

/**
 * @author mgergalov
 */
data class Trade(
        val tradeId: String,
        val exchange: String,
        val timestamp: String,
        val type: TradeType,
        val pair: TokensPair,
        val baseAmount: BigDecimal,
        val quoteAmount: BigDecimal,
        val spotPrice: BigDecimal,
        val reverted: Boolean,
        val epoch_h: Long
) {

    companion object {
        fun of(tradeId: String,
               exchange: String,
               timestamp: Long,
               type: TradeType,
               baseAmount: BigDecimal,
               quoteAmount: BigDecimal,
               spotPrice: BigDecimal,
               tokensPairInitializer: TokensPairInitializer
        ): Trade {
            val resType: TradeType
            val resBaseAmount: BigDecimal
            val resQuoteAmount: BigDecimal
            val resSpotPrice: BigDecimal
            if (tokensPairInitializer.reverted) {
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
            return Trade(tradeId, exchange, Instant.ofEpochSecond(timestamp).toString(), type, TokensPair(tokensPairInitializer.pair.base, tokensPairInitializer.pair.quote),
                    resBaseAmount, resQuoteAmount, resSpotPrice, tokensPairInitializer.reverted, timestamp/60/60)
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