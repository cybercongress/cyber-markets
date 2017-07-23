package fund.cyber.markets.exchanges.bitfinex

import fund.cyber.markets.configuration.SCHEDULER_POOL_SIZE
import fund.cyber.markets.exchanges.ExchangeMetadataService
import fund.cyber.markets.helpers.createExchange
import fund.cyber.markets.model.ExchangeMetadata
import fund.cyber.markets.model.TokensPair
import fund.cyber.markets.model.bitfinex
import org.knowm.xchange.ExchangeFactory
import org.knowm.xchange.bitfinex.v1.BitfinexExchange
import org.knowm.xchange.bitfinex.v1.service.BitfinexMarketDataService
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap


class BitfinexMetadata(
        val channelSymbolForTokensPair: Map<String, TokensPair>,
        //do not cache it, each time you subscribe channel, a new id is provided
        val tradesChannelIdForTokensPair: Map<Int, TokensPair>
) : ExchangeMetadata(bitfinex, "wss://api.bitfinex.com/ws/2")


@Component
open class BitfinexExchangeMetadataService : ExchangeMetadataService<BitfinexMetadata>(bitfinex) {

    // populated during receiving channel subscription result message
    private val tradesChannelIdForTokensPair: MutableMap<Int, TokensPair> = ConcurrentHashMap(32, 0.75f, SCHEDULER_POOL_SIZE)
    private val channelSymbolForTokensPair: MutableMap<String, TokensPair> = ConcurrentHashMap(32, 0.75f, 2)

    override val metadata = BitfinexMetadata(channelSymbolForTokensPair, tradesChannelIdForTokensPair)

    override fun initializeMetadata() {

        val bitfinex = ExchangeFactory.INSTANCE.createExchange<BitfinexExchange>()
        val updatedChannelSymbolForTokensPair = bitfinex.getChannelSymbolForTokensPair()

        channelSymbolForTokensPair.putAll(updatedChannelSymbolForTokensPair)
    }

    override fun updateMetadata() {
        //todo implement
    }

    fun updatesTradeChannelId(channelId: Int, tokensPair: TokensPair) {
        tradesChannelIdForTokensPair.put(channelId, tokensPair)
    }
}

/**
 * Warning! Invokes http request to obtain data.
 */
fun BitfinexExchange.getChannelSymbolForTokensPair(): Map<String, TokensPair> {

    remoteInit()
    val exchangeSymbols = (marketDataService as BitfinexMarketDataService).getExchangeSymbols()

    val channelSymbolForTokensPair: MutableMap<String, TokensPair> = java.util.HashMap()
    exchangeSymbols.forEach { pair ->
        val bitfinexSymbol = "t" + (pair.base.currencyCode + pair.counter.currencyCode).toUpperCase()
        channelSymbolForTokensPair.put(bitfinexSymbol, TokensPair(pair.base.currencyCode, pair.counter.currencyCode))
    }
    return channelSymbolForTokensPair
}
