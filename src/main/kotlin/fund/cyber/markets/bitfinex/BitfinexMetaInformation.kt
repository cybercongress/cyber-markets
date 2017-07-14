package fund.cyber.markets.bitfinex

import fund.cyber.markets.model.TokensPair
import org.springframework.stereotype.Component

/**
 * Poloniex exchange meta information holder.
 *
 * @author hleb.albau@gmail.com
 */
@Component
open class BitfinexMetaInformation {

    var channelSymbolForTokensPair: Map<String, TokensPair> = HashMap()

    // populated during parsing channel subscription result message
    // each time you subscribe channel, a new id is provided -> no possibility to cache it
    val tradesChannelIdForTokensPair: MutableMap<Int, TokensPair> = HashMap()
}
