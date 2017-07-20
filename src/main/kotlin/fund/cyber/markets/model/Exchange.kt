package fund.cyber.markets.model

import fund.cyber.markets.configuration.WS_CONNECTION_IDLE_TIMEOUT
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI
import java.time.Instant

/**
 * Exchange model elements.
 *
 * @author hleb.albau@gmail.com
 */

val bitfinex = "Bitfinex"
val poloniex = "Poloniex"
val hitbtc = "HitBtc"


open class TokensPair(
        val base: String,
        val quote: String
) {
    fun label(): String {
        return base + "/" + quote
    }
}

open class ExchangeMetadata(
        val exchange: String,
        val wsAddress: String
) {

    fun wsUri(): URI {
        return UriComponentsBuilder.fromUriString(wsAddress).build().encode().toUri()
    }
}

/**
 * If end time is not specified, than gap is "open"(in progress). Otherwise is "closed".
 *
 * @property precision describe possible start time deviation. When handle gap, keep in mind, that start time is not
 *  actual connection lost time, but time, when we notice that connection is lost. [WS_CONNECTION_IDLE_TIMEOUT] is
 *  default guaranteed period of alive connection. Unit -> Seconds.
 *
 */
open class HistoryGap(
        val exchange: String,
        val startTime: Long = Instant.now().epochSecond,
        var endTime: Long? = null,
        val precision: Long = WS_CONNECTION_IDLE_TIMEOUT
)