package fund.cyber.markets.model

import com.datastax.driver.mapping.annotations.ClusteringColumn
import com.datastax.driver.mapping.annotations.Frozen
import com.datastax.driver.mapping.annotations.PartitionKey
import com.datastax.driver.mapping.annotations.Table
import fund.cyber.markets.dto.TokensPair
import java.math.BigDecimal
import java.util.*

@Table(keyspace = "markets", name = "ticker",
        readConsistency = "QUORUM", writeConsistency = "QUORUM",
        caseSensitiveKeyspace = false, caseSensitiveTable = false)
data class Ticker(

        @PartitionKey(0) @Frozen var pair: TokensPair?,
        @PartitionKey(1) var windowDuration: Long,
        @ClusteringColumn(0) var exchange: String?,
        @ClusteringColumn(1) var timestampTo: Date?,
        var timestampFrom: Date?,

        var price: BigDecimal,
        var open: BigDecimal,
        var close: BigDecimal,

        var minPrice: BigDecimal?,
        var maxPrice: BigDecimal?,

        var baseAmount: BigDecimal,
        var quoteAmount: BigDecimal,

        var tradeCount: Long
) {

    constructor(windowDuration: Long) :
            this(null, windowDuration, null, null, null, BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0)

    fun add(trade: Trade): Ticker {

        if (!validTrade(trade)) {
            return this
        }

        if (isNewTicker()) {
            pair = trade.pair
            exchange = trade.exchange
            open = trade.quoteAmount.div(trade.baseAmount)
            minPrice = open
            maxPrice = open
        }

        val tradePrice = trade.quoteAmount.div(trade.baseAmount)

        quoteAmount = quoteAmount.plus(trade.quoteAmount)
        baseAmount = baseAmount.plus(trade.baseAmount)

        minPrice = minPrice?.min(tradePrice)
        maxPrice = maxPrice?.max(tradePrice)

        close = tradePrice

        tradeCount++

        return this
    }

    fun add(ticker: Ticker): Ticker {

        if (isNewTicker()) {
            pair = ticker.pair
            exchange = ticker.exchange
            open = ticker.open
            minPrice = open
            maxPrice = open
        }

        quoteAmount = quoteAmount.plus(ticker.quoteAmount)
        baseAmount = baseAmount.plus(ticker.baseAmount)

        minPrice = minPrice?.min(ticker.minPrice)
        maxPrice = maxPrice?.max(ticker.maxPrice)

        close = ticker.close
        tradeCount += ticker.tradeCount

        return this
    }

    fun minus(ticker: Ticker): Ticker {
        quoteAmount = quoteAmount.minus(ticker.quoteAmount)
        baseAmount = baseAmount.minus(ticker.baseAmount)

        tradeCount -= ticker.tradeCount

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

    fun setTimestamps(millisFrom: Long, millisTo: Long) : Ticker {
        timestampFrom = Date(millisFrom)
        timestampTo = Date(millisTo)

        return this
    }

    private fun validTrade(trade: Trade): Boolean {
        return !(trade.quoteAmount.compareTo(BigDecimal.ZERO) == 0 || trade.baseAmount.compareTo(BigDecimal.ZERO) == 0)
    }

    private fun isNewTicker(): Boolean {
        return exchange == null || pair == null
    }

}