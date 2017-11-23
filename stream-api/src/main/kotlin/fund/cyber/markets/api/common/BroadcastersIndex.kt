package fund.cyber.markets.api.common

import fund.cyber.markets.api.orders.OrdersBroadcaster
import fund.cyber.markets.api.tickers.TickersBroadcaster
import fund.cyber.markets.api.trades.TradesBroadcaster
import fund.cyber.markets.dto.TokensPair
import fund.cyber.markets.model.Order
import fund.cyber.markets.model.Trade
import fund.cyber.markets.tickers.model.Ticker
import kotlinx.coroutines.experimental.channels.Channel
import java.util.concurrent.ConcurrentHashMap

class TradesBroadcastersIndex : BroadcastersIndex<Trade, TradesBroadcaster>() {
    override fun newChannel(exchange: String, pairInitializer: TokensPair, channel: Channel<Trade>) {
        val broadcaster = TradesBroadcaster(channel)
        index.put(BroadcasterDefinition(pairInitializer, exchange, windowDuration), broadcaster)
    }
}

class OrdersBroadcastersIndex : BroadcastersIndex<List<Order>, OrdersBroadcaster>() {
    override fun newChannel(exchange: String, pairInitializer: TokensPair, channel: Channel<List<Order>>) {
        val broadcaster = OrdersBroadcaster(channel)
        index.put(BroadcasterDefinition(pairInitializer, exchange, windowDuration), broadcaster)
    }
}

class TickersBroadcastersIndex : BroadcastersIndex<Ticker, TickersBroadcaster>() {
    override fun newChannel(exchange: String, pairInitializer: TokensPairInitializer, windowDuration: Long, channel: Channel<Ticker>) {
        val broadcaster = TickersBroadcaster(channel)
        index.put(BroadcasterDefinition(pairInitializer, exchange, windowDuration), broadcaster)
    }
}

data class BroadcasterDefinition(val tokensPair: TokensPairInitializer,
                                 val exchange: String,
                                 val windowDuration: Long)

abstract class BroadcastersIndex<T, B : Broadcaster> : ChannelsIndexUpdateListener<T> {

    protected val index: MutableMap<BroadcasterDefinition, B> = ConcurrentHashMap()

    fun broadcastersFor(pairInitializers: List<TokensPairInitializer>, exchanges: List<String>, windowDurations: List<Long> = emptyList()): Collection<B> {

        return index.filter { (definition, _) ->
            (pairInitializers.isEmpty() || pairInitializers.contains(definition.tokensPair))
            && (exchanges.isEmpty() || exchanges.contains(definition.exchange))
            && (windowDurations.isEmpty() || definition.windowDuration < 0 || windowDurations.contains(definition.windowDuration))
        }.map { (_, broadcaster) -> broadcaster }

    }

    fun getAllPairs(): Collection<TokensPairInitializer> {
        return index.map { (definition, _) -> definition.tokensPair }
    }

    fun getAllExchangesWithPairs(): Map<String, List<TokensPair>> {
        return index
                .map { (definition, _) -> definition.exchange to getAllPairsForExchange(definition.exchange) }
                .toMap()
    }

    private fun getAllPairsForExchange(exchange: String): List<TokensPairInitializer> {
        return index
                .filter { (definition, _) -> definition.exchange == exchange }
                .map { (definition, _) -> definition.tokensPair }
    }
}