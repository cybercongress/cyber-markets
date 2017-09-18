package fund.cyber.markets.model

open class TokensPair(firstCurrency: String, secondCurrency: String) {

    val base: String
    val quote: String
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
                this.base = secondCurrency
                this.quote = firstCurrency
            } else {
                this.base = firstCurrency
                this.quote = secondCurrency
            }
        } else if (firstImportance >= 0 || secondImportance >= 0) {
            if (firstImportance >= 0) {
                this.base = secondCurrency
                this.quote = firstCurrency
            } else {
                this.base = firstCurrency
                this.quote = secondCurrency
            }
        } else {
            if (firstCurrency.compareTo(secondCurrency, true) < 0) {
                this.base = firstCurrency
                this.quote = secondCurrency
            } else {
                this.base = secondCurrency
                this.quote = firstCurrency
            }
        }
    }

    fun label(delimiter: String = "/"): String {
        return base + delimiter + quote
    }

    override fun hashCode(): Int {
        var result = base.hashCode()
        result = 31 * result + quote.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as TokensPair

        if (base != other.base) return false
        if (quote != other.quote) return false

        return true
    }

    companion object {
        fun fromLabel(label: String, delimiter: String = "/"): TokensPair {
            return TokensPair(label.substringBefore(delimiter), label.substringAfter(delimiter))
        }
    }

}