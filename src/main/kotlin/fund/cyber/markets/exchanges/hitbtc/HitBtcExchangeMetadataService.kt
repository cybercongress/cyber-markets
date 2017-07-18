package fund.cyber.markets.exchanges.hitbtc

import fund.cyber.markets.model.ExchangeMetadata
import fund.cyber.markets.model.TokensPair
import fund.cyber.markets.model.hitbtc
import fund.cyber.markets.service.ExchangeMetadataService
import fund.cyber.markets.storage.RethinkDbService
import org.knowm.xchange.ExchangeFactory
import org.knowm.xchange.hitbtc.HitbtcExchange
import org.knowm.xchange.hitbtc.service.HitbtcMarketDataService
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component
import java.math.BigDecimal

// pairs of currency and tokens
class HitBtcTokensPair(
        val symbol: String,
        val lotSize: BigDecimal,
        val priceStep: BigDecimal,
        base: String,
        quote: String
) : TokensPair(base, quote)

class HitBtcMetadata(
        exchange: String = hitbtc,
        wsAddress: String = "ws://api.hitbtc.com:80",
        var channelSymbolForTokensPair: Map<String, HitBtcTokensPair> = HashMap()
) : ExchangeMetadata(exchange, wsAddress)


@Component
open class HitBtcExchangeMetadataService(
        eventPublisher: ApplicationEventPublisher, rethinkDbService: RethinkDbService
) : ExchangeMetadataService<HitBtcMetadata>(
        exchange = hitbtc, metadataClass = HitBtcMetadata::class.java,
        eventPublisher = eventPublisher, rethinkDbService = rethinkDbService
) {


    override fun getMetadataFromExchange(): HitBtcMetadata {

        val hitbtc = ExchangeFactory.INSTANCE.createExchange(HitbtcExchange::class.java.name) as HitbtcExchange
        val marketDataService = hitbtc.marketDataService as HitbtcMarketDataService

        val channelSymbolForTokensPair = marketDataService.getHitbtcSymbols().hitbtcSymbols
                .map { symbols -> HitBtcTokensPair(
                            base = symbols.commodity, quote = symbols.currency,
                            symbol = (symbols.commodity + symbols.currency).toUpperCase(),
                            lotSize = symbols.lot, priceStep = symbols.step)}
                .associateBy { tokensPair: HitBtcTokensPair -> tokensPair.symbol }

        return HitBtcMetadata(channelSymbolForTokensPair = channelSymbolForTokensPair)
    }
}