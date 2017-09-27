package fund.cyber.markets.dto

/**
 * @author mgergalov
 */
class TokensPairDto(
        val base: String,
        val quote: String
){
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TokensPairDto) return false

        if (base != other.base) return false
        if (quote != other.quote) return false

        return true
    }

    override fun hashCode(): Int {
        var result = base.hashCode()
        result = 31 * result + quote.hashCode()
        return result
    }

    override fun toString(): String {
        return "TokensPairDto(base='$base', quote='$quote')"
    }
}