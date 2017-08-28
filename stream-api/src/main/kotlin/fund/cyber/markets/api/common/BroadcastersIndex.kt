package fund.cyber.markets.api.common

import fund.cyber.markets.api.orders.OrdersBroadcaster
import fund.cyber.markets.api.trades.TradesBroadcaster
import fund.cyber.markets.model.Order
import fund.cyber.markets.model.TokensPair
import fund.cyber.markets.model.Trade
import kotlinx.coroutines.experimental.channels.Channel
import java.util.LinkedList
import java.util.concurrent.ConcurrentHashMap

class TradesBroadcastersIndex : BroadcastersIndex<Trade>() {
    override fun newChannel(exchange: String, pair: TokensPair, channel: Channel<Trade>) {
        val broadcaster = TradesBroadcaster(channel)
        index.getOrPut(pair, { ConcurrentHashMap() }).put(exchange, broadcaster)
    }
}

class OrdersBroadcastersIndex : BroadcastersIndex<List<Order>>() {
    override fun newChannel(exchange: String, pair: TokensPair, channel: Channel<List<Order>>) {
        val broadcaster = OrdersBroadcaster(channel)
        index.getOrPut(pair, { ConcurrentHashMap() }).put(exchange, broadcaster)
    }
}

abstract class BroadcastersIndex<T> : ChannelsIndexUpdateListener<T> {

    protected val index: MutableMap<TokensPair, MutableMap<String, Broadcaster>> = ConcurrentHashMap()

    fun broadcastersFor(pairs: List<TokensPair>, exchanges: List<String>): Collection<Broadcaster> {
        return when {
            !pairs.isEmpty() && !exchanges.isEmpty() -> index
                    .filter { (pair, _) -> pairs.contains(pair) }
                    .flatMap { (_, pairIndex) -> pairIndex.entries }
                    .filter { (exchange, _) -> exchanges.contains(exchange) }
                    .map { (_, broadcaster) -> broadcaster }
            !pairs.isEmpty() -> index
                    .filter { (pair, _) -> pairs.contains(pair) }
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