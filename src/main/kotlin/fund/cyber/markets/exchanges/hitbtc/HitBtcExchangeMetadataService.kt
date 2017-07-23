package fund.cyber.markets.exchanges.hitbtc

import fund.cyber.markets.exchanges.ExchangeMetadataService
import fund.cyber.markets.helpers.createExchange
import fund.cyber.markets.model.ExchangeMetadata
import fund.cyber.markets.model.TokensPair
import fund.cyber.markets.model.hitbtc
import org.knowm.xchange.ExchangeFactory
import org.knowm.xchange.hitbtc.HitbtcExchange
import org.knowm.xchange.hitbtc.service.HitbtcMarketDataService
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.util.concurrent.ConcurrentHashMap


// pairs of currency and tokens
class HitBtcTokensPair(
        val symbol: String,
        val lotSize: BigDecimal,
        val priceStep: BigDecimal,
        base: String,
        quote: String
) : TokensPair(base, quote)


class HitBtcMetadata(
        val channelSymbolForTokensPair: Map<String, HitBtcTokensPair>
) : ExchangeMetadata(hitbtc, "ws://api.hitbtc.com:80")


@Component
open class HitBtcExchangeMetadataService : ExchangeMetadataService<HitBtcMetadata>(exchange = hitbtc) {

    private val channelSymbolForTokensPair: MutableMap<String, HitBtcTokensPair> = ConcurrentHashMap(16, 0.75f, 2)
    override val metadata = HitBtcMetadata(channelSymbolForTokensPair)

    override fun initializeMetadata() {
        val hitbtc = ExchangeFactory.INSTANCE.createExchange<HitbtcExchange>()
        val marketDataService = hitbtc.marketDataService as HitbtcMarketDataService

        val updatedChannelSymbolForTokensPair = marketDataService.getHitbtcSymbols().hitbtcSymbols
                .map { symbols ->
                    HitBtcTokensPair(
                            base = symbols.commodity, quote = symbols.currency,
                            symbol = (symbols.commodity + symbols.currency).toUpperCase(),
                            lotSize = symbols.lot, priceStep = symbols.step)
                }
                .associateBy { tokensPair: HitBtcTokensPair -> tokensPair.symbol }

        channelSymbolForTokensPair.putAll(updatedChannelSymbolForTokensPair)
    }

    override fun updateMetadata() {
        //todo implement
    }
}