package fund.cyber.markets.exchanges.poloniex

import fund.cyber.markets.exchanges.ExchangeMetadataService
import fund.cyber.markets.model.ExchangeMetadata
import fund.cyber.markets.model.TokensPair
import fund.cyber.markets.model.poloniex
import org.knowm.xchange.ExchangeFactory
import org.knowm.xchange.poloniex.PoloniexExchange
import org.knowm.xchange.poloniex.service.PoloniexMarketDataService
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

class PoloniexMetadata(
        val channelIdForTokensPairs: Map<Int, TokensPair>
) : ExchangeMetadata(poloniex, "wss://api2.poloniex.com/")


@Component
open class PoloniexExchangeMetadataService : ExchangeMetadataService<PoloniexMetadata>(poloniex) {

    private val channelIdForTokensPairs: MutableMap<Int, TokensPair> = ConcurrentHashMap(16, 0.75f, 2)
    override val metadata = PoloniexMetadata(channelIdForTokensPairs)

    override fun initializeMetadata() {
        val poloniex = ExchangeFactory.INSTANCE.createExchange(PoloniexExchange::class.java.name) as PoloniexExchange
        val actualChannelIdForTokensPairs = poloniex.getTokensPairsWithChannelIds()
        channelIdForTokensPairs.putAll(actualChannelIdForTokensPairs)

    }

    override fun updateMetadata() {
        //todo implement
    }
}

/**
 * Warning! Invokes http request to obtain data.
 */
fun PoloniexExchange.getTokensPairsWithChannelIds(): Map<Int, TokensPair> {

    remoteInit()
    val poloniexTickers = (marketDataService as PoloniexMarketDataService).getAllPoloniexTickers()

    val channelIdForTokensPair: MutableMap<Int, TokensPair> = HashMap()
    poloniexTickers?.forEach { tokensPairSymbol, metaInfo ->
        val pairId = metaInfo.additionalProperties["id"] as Int
        channelIdForTokensPair.put(pairId, toTokensPair(tokensPairSymbol))
    }
    return channelIdForTokensPair
}

private fun toTokensPair(tokensPairSymbol: String): TokensPair {
    val split = tokensPairSymbol.split("_")
    return TokensPair(split[0], split[1])
}