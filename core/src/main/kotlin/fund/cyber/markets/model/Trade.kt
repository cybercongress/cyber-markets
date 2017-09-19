package fund.cyber.markets.model

import java.math.BigDecimal

/**
 * @author mgergalov
 */
open class Trade(
        val tradeId: String,
        val exchange: String,
        val timestamp: Long,
        type: TradeType,
        baseAmount: BigDecimal,
        quoteAmount: BigDecimal,
        spotPrice: BigDecimal,
        tokensPair: TokensPair
){

    val baseToken : String = tokensPair.base
    val quoteToken : String = tokensPair.quote
    val type : TradeType
    val baseAmount : BigDecimal
    val quoteAmount : BigDecimal
    val spotPrice : BigDecimal
    val reverted : Boolean = tokensPair.reverted

    init {
        if(tokensPair.reverted) {
            this.type = revertType(type)
            this.baseAmount = quoteAmount
            this.quoteAmount = baseAmount
            this.spotPrice = this.quoteAmount / this.baseAmount
        } else {
            this.type = type
            this.baseAmount = baseAmount
            this.quoteAmount = quoteAmount
            this.spotPrice = spotPrice
        }
    }

    private fun revertType(type: TradeType) : TradeType {
        return when (type){
            TradeType.BUY -> TradeType.SELL
            TradeType.SELL -> TradeType.BUY
            else -> type
        }
    }

    override fun toString(): String {
        return "Trade(tradeId='$tradeId', exchange='$exchange', timestamp=$timestamp, baseToken='$baseToken', quoteToken='$quoteToken', type=$type, baseAmount=$baseAmount, quoteAmount=$quoteAmount, spotPrice=$spotPrice, reverted=$reverted)"
    }

}