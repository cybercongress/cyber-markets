package fund.cyber.markets.api.common

import fund.cyber.markets.api.orders.OrdersBroadcaster
import fund.cyber.markets.api.trades.TradesBroadcaster
import fund.cyber.markets.dto.TokensPair
import fund.cyber.markets.model.Order
import fund.cyber.markets.model.TokensPairInitializer
import fund.cyber.markets.model.Trade
import kotlinx.coroutines.experimental.channels.Channel
import sun.tools.jstat.Token
import java.util.concurrent.ConcurrentHashMap

class TradesBroadcastersIndex : BroadcastersIndex<Trade, TradesBroadcaster>() {
    override fun newChannel(exchange: String, pairInitializer: TokensPair, channel: Channel<Trade>) {
        val broadcaster = TradesBroadcaster(channel)
        index.getOrPut(pairInitializer, { ConcurrentHashMap() }).put(exchange, broadcaster)
    }
}

class OrdersBroadcastersIndex : BroadcastersIndex<List<Order>, OrdersBroadcaster>() {
    override fun newChannel(exchange: String, pairInitializer: TokensPair, channel: Channel<List<Order>>) {
        val broadcaster = OrdersBroadcaster(channel)
        index.getOrPut(pairInitializer, { ConcurrentHashMap() }).put(exchange, broadcaster)
    }
}

abstract class BroadcastersIndex<T, B : Broadcaster> : ChannelsIndexUpdateListener<T> {

    protected val index: MutableMap<TokensPair, MutableMap<String, B>> = ConcurrentHashMap()

    fun broadcastersFor(pairInitializers: List<TokensPair>, exchanges: List<String>): Collection<B> {
        return when {
            !pairInitializers.isEmpty() && !exchanges.isEmpty() -> index
                    .filter { (pair, _) -> pairInitializers.contains(pair) }
                    .flatMap { (_, pairIndex) -> pairIndex.entries }
                    .filter { (exchange, _) -> exchanges.contains(exchange) }
                    .map { (_, broadcaster) -> broadcaster }
            !pairInitializers.isEmpty() -> index
                    .filter { (pair, _) -> pairInitializers.contains(pair) }
                    .flatMap { (_, pairIndex) -> pairIndex.values }
            !exchanges.isEmpty() -> index
                    .flatMap { (_, pairIndex) -> pairIndex.entries }
                    .filter { (exchange, _) -> exchanges.contains(exchange) }
                    .map { (_, broadcaster) -> broadcaster }
            else -> index
                    .flatMap { (_, pairIndex) -> pairIndex.entries }
                    .map { (_, broadcaster) -> broadcaster }
        }
    }

    fun getAllPairs(): Collection<TokensPair> {
        return index.keys
    }

    fun getAllExchangesWithPairs(): Map<String, List<TokensPair>> {
        return index
                .flatMap {(_, pairIndex) -> pairIndex.entries }
                .map { c -> c.key to getAllPairsForExchange(c.key) }
                .toMap()
    }

    private fun getAllPairsForExchange(exchange: String): List<TokensPair> {
        return index.filter { el -> el.value.keys.contains(exchange) }.map { el -> el.key }
    }
}