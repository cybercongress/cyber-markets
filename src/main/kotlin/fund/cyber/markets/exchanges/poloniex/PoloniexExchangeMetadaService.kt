package fund.cyber.markets.exchanges.poloniex

import fund.cyber.markets.model.ExchangeMetadata
import fund.cyber.markets.model.TokensPair
import fund.cyber.markets.model.poloniex
import fund.cyber.markets.service.ExchangeMetadataService
import fund.cyber.markets.storage.RethinkDbService
import org.knowm.xchange.ExchangeFactory
import org.knowm.xchange.poloniex.PoloniexExchange
import org.knowm.xchange.poloniex.service.PoloniexMarketDataService
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

class PoloniexMetadata(
        exchange: String = poloniex,
        wsAddress: String = "wss://api2.poloniex.com/",
        var channelIdForTokensPairs: Map<Int, TokensPair> = HashMap()
) : ExchangeMetadata(exchange, wsAddress)


@Component
open class PoloniexExchangeMetadataService(
        eventPublisher: ApplicationEventPublisher, rethinkDbService: RethinkDbService
) : ExchangeMetadataService<PoloniexMetadata>(
        exchange = poloniex, metadataClass = PoloniexMetadata::class.java,
        eventPublisher = eventPublisher, rethinkDbService = rethinkDbService
) {

    override fun getMetadataFromExchange(): PoloniexMetadata {

        val poloniex = ExchangeFactory.INSTANCE.createExchange(PoloniexExchange::class.java.name) as PoloniexExchange
        val channelIdForTokensPairs = poloniex.getTokensPairsWithChannelIds()
        return PoloniexMetadata(channelIdForTokensPairs = channelIdForTokensPairs)
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