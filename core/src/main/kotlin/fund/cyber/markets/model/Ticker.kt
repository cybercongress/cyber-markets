package fund.cyber.markets.model

import com.datastax.driver.mapping.annotations.ClusteringColumn
import com.datastax.driver.mapping.annotations.Frozen
import com.datastax.driver.mapping.annotations.PartitionKey
import com.datastax.driver.mapping.annotations.Table
import com.datastax.driver.mapping.annotations.Transient
import com.fasterxml.jackson.annotation.JsonIgnore
import fund.cyber.markets.dto.TokensPair
import java.math.BigDecimal
import java.util.*

@Table(keyspace = "markets", name = "ticker",
        readConsistency = "QUORUM", writeConsistency = "QUORUM",
        caseSensitiveKeyspace = false, caseSensitiveTable = false)
data class Ticker(

        @ClusteringColumn(0)
        var exchange: String?,

        @Frozen
        @PartitionKey(0)
        var tokensPair: TokensPair?,
        var timestampFrom: Date?,

        @ClusteringColumn(1)
        var timestampTo: Date?,

        @PartitionKey(1)
        var windowDuration: Long,
        var baseAmount: BigDecimal,
        var quoteAmount: BigDecimal,
        var price: BigDecimal,
        var minPrice: BigDecimal?,
        var maxPrice: BigDecimal?,
        var tradeCount: Long
) {

    constructor(windowDuration: Long) : this(null, null, null, null, windowDuration, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, null, null, 0)
    constructor() : this(-1L)

    @Transient
    @JsonIgnore
    private val priceMap = mutableMapOf<BigDecimal, Int>()
    @Transient
    @JsonIgnore
    private val priceSet = TreeSet<BigDecimal>()

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

        saveMinMaxPrices(minPrice!!, maxPrice!!)

        tradeCount += ticker.tradeCount

        return this
    }

    fun minus(ticker: Ticker): Ticker {
        quoteAmount = quoteAmount.minus(ticker.quoteAmount)
        baseAmount = baseAmount.minus(ticker.baseAmount)

        restoreMinMaxPrices(ticker)

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

    private fun saveMinMaxPrices(minPrice: BigDecimal, maxPrice: BigDecimal) {
        priceMap.getOrPut(minPrice, { 0 } )
        priceMap.getOrPut(maxPrice, { 0 } )

        priceMap.put(minPrice, priceMap[minPrice]!! + 1 )
        priceMap.put(maxPrice, priceMap[maxPrice]!! + 1 )

        priceSet.add(minPrice)
        priceSet.add(maxPrice)
    }

    private fun restoreMinMaxPrices(ticker: Ticker) {
        restorePrice(ticker.minPrice!!)
        restorePrice(ticker.maxPrice!!)

        try {
            this.minPrice = priceSet.first()
            this.maxPrice = priceSet.last()
        } catch (e: Exception) {
            this.minPrice = BigDecimal(0)
            this.maxPrice = BigDecimal(0)
        }
    }

    private fun restorePrice(price: BigDecimal) {
        val priceCount = priceMap[price]
        if (priceCount != null && priceCount > 1) {
            priceMap.put(price, priceCount - 1)
        } else {
            priceSet.remove(price)
        }
    }

    private fun validTrade(trade: Trade): Boolean {
        return !(trade.quoteAmount.compareTo(BigDecimal.ZERO) == 0 || trade.baseAmount.compareTo(BigDecimal.ZERO) == 0)
    }

}