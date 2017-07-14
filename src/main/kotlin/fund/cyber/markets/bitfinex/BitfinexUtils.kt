package fund.cyber.markets.bitfinex

import fund.cyber.markets.model.TokensPair
import org.knowm.xchange.bitfinex.v1.BitfinexExchange
import org.knowm.xchange.bitfinex.v1.service.BitfinexMarketDataService
import java.util.*


val WS_ADDRESS = "wss://api.bitfinex.com/ws/2"



/**
 * Warning! Invokes http request to obtain data.
 */
fun BitfinexExchange.getChannelSymbolForTokensPair(): Map<String, TokensPair> {

    remoteInit()
    val exchangeSymbols = (marketDataService as BitfinexMarketDataService).getExchangeSymbols()

    val channelSymbolForTokensPair: MutableMap<String, TokensPair> = HashMap()
    exchangeSymbols.forEach { pair ->
        val bitfinexSymbol = "t" + (pair.base.currencyCode + pair.counter.currencyCode).toUpperCase()
        channelSymbolForTokensPair.put(bitfinexSymbol, TokensPair(pair.base.currencyCode, pair.counter.currencyCode))
    }
    return channelSymbolForTokensPair
}

