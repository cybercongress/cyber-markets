package fund.cyber.markets.poloniex

import fund.cyber.markets.model.CurrencyPair
import org.springframework.stereotype.Component

/**
 * Poloniex exchange meta information holder.
 *
 * @author hleb.albau@gmail.com
 */
@Component
open class PoloniexMetaInformation() {
    var channelIdForCurrencyPair: Map<Int, CurrencyPair> = HashMap()
}
