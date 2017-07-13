package fund.cyber.markets.bitfinex

import com.fasterxml.jackson.databind.ObjectMapper
import fund.cyber.markets.model.ExchangeItems
import org.springframework.stereotype.Component

/**
 *   Bitfinex ws message parser.
 *
 *
 *  @author hleb.albau@gmail.com
 */
@Component
open class BitfinexMessageParser(
        val bitfinexMetaInformation: BitfinexMetaInformation
) {

    private val mapper = ObjectMapper()

    fun parseMessage(message: String): ExchangeItems {
        return ExchangeItems()
    }
}