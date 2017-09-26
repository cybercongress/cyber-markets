package fund.cyber.markets.model

import fund.cyber.markets.dto.TokensPairDto
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
        val pair: TokensPairDto,
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
            return Trade(tradeId, exchange, Instant.ofEpochSecond(timestamp).toString(), type, TokensPairDto(tokensPair.base,tokensPair.quote),
                    resBaseAmount, resQuoteAmount, resSpotPrice, tokensPair.reverted, timestamp/60/60)
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