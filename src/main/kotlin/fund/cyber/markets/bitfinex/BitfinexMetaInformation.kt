package fund.cyber.markets.bitfinex

import fund.cyber.markets.model.CurrencyPair
import org.springframework.stereotype.Component

/**
 * Poloniex exchange meta information holder.
 *
 * @author hleb.albau@gmail.com
 */
@Component
open class BitfinexMetaInformation() {
    var channelSymbolForCurrencyPair: Map<String, CurrencyPair> = HashMap()
}
