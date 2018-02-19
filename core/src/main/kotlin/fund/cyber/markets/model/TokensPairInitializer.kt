package fund.cyber.markets.model

open class TokensPairInitializer(firstCurrency: String, secondCurrency: String) {

    val pair: TokensPair
    var reverted: Boolean = false
    var fiatDictionary = listOf(
            "USD", "EUR", "GBP"
    )
    var backedCryptoDictionary = listOf(
            "USDT"
    )
    var cryptoDictionary = listOf(
            "BTC", "ETH", "XMR"
    )

    init {
        val fullDictionary = ArrayList(fiatDictionary)
        fullDictionary.addAll(backedCryptoDictionary)
        fullDictionary.addAll(cryptoDictionary)

        val firstImportance = fullDictionary.indexOf(firstCurrency)
        val secondImportance = fullDictionary.indexOf(secondCurrency)

        if (firstImportance >= 0 && secondImportance >= 0) {
            if (firstImportance < secondImportance) {
                this.pair = TokensPair(secondCurrency, firstCurrency)
                this.reverted =  true
            } else {
                this.pair = TokensPair(firstCurrency, secondCurrency)
            }
        } else if (firstImportance >= 0 || secondImportance >= 0) {
            if (firstImportance >= 0) {
                this.pair = TokensPair(secondCurrency, firstCurrency)
                this.reverted =  true
            } else {
                this.pair = TokensPair(firstCurrency, secondCurrency)
            }
        } else {
            if (firstCurrency.compareTo(secondCurrency, true) < 0) {
                this.pair = TokensPair(firstCurrency, secondCurrency)
            } else {
                this.pair = TokensPair(secondCurrency, firstCurrency)
                this.reverted =  true
            }
        }
    }

    fun label(delimiter: String = "/"): String {
        return pair.base + delimiter + pair.quote
    }

    override fun hashCode(): Int {
        var result = pair.base.hashCode()
        result = 31 * result + pair.quote.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as TokensPairInitializer

        if (pair.base != other.pair.base) return false
        if (pair.quote != other.pair.quote) return false

        return true
    }

    companion object {
        fun fromLabel(label: String, delimiter: String = "/"): TokensPairInitializer {
            return TokensPairInitializer(label.substringBefore(delimiter), label.substringAfter(delimiter))
        }
    }

}