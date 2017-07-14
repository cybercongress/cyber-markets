package fund.cyber.markets.bitfinex

import fund.cyber.markets.model.CurrencyPair
import org.springframework.stereotype.Component

/**
 * Poloniex exchange meta information holder.
 *
 * @author hleb.albau@gmail.com
 */
@Component
open class BitfinexMetaInformation {

    var channelSymbolForCurrencyPair: Map<String, CurrencyPair> = HashMap()

    // populated during parsing channel subscription result message
    // each time you subscribe channel, a new id is provided -> no possibility to cache it
    val tradesChannelIdForCurrencyPair: MutableMap<Int, CurrencyPair> = HashMap()
}
