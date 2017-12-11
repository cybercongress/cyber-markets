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
    override fun newChannel(exchange: String, tokensPair: TokensPair, windowDuration: Long, channel: Channel<Trade>) {
        val broadcaster = TradesBroadcaster(channel)
        index.put(BroadcasterDefinition(tokensPair, exchange, windowDuration), broadcaster)
    }
}

class OrdersBroadcastersIndex : BroadcastersIndex<List<Order>, OrdersBroadcaster>() {
    override fun newChannel(exchange: String, tokensPair: TokensPair, windowDuration: Long, channel: Channel<List<Order>>) {
        val broadcaster = OrdersBroadcaster(channel)
        index.put(BroadcasterDefinition(tokensPair, exchange, windowDuration), broadcaster)
    }
}

class TickersBroadcastersIndex : BroadcastersIndex<Ticker, TickersBroadcaster>() {
    override fun newChannel(exchange: String, tokensPair: TokensPair, windowDuration: Long, channel: Channel<Ticker>) {
        val broadcaster = TickersBroadcaster(channel)
        index.put(BroadcasterDefinition(tokensPair, exchange, windowDuration), broadcaster)
    }
}

data class BroadcasterDefinition(val tokensPair: TokensPair,
                                 val exchange: String,
                                 val windowDuration: Long)

abstract class BroadcastersIndex<T, B : Broadcaster> : ChannelsIndexUpdateListener<T> {

    protected val index: MutableMap<BroadcasterDefinition, B> = ConcurrentHashMap()

    fun broadcastersFor(tokenPairs: List<TokensPair>, exchanges: List<String>, windowDurations: List<Long> = emptyList()): Collection<B> {

        return index.filter { (definition, _) ->
            (tokenPairs.isEmpty() || tokenPairs.contains(definition.tokensPair))
            && (exchanges.isEmpty() || exchanges.contains(definition.exchange))
            && (windowDurations.isEmpty() || definition.windowDuration < 0 || windowDurations.contains(definition.windowDuration))
        }.map { (_, broadcaster) -> broadcaster }

    }

    fun getAllPairs(): Collection<TokensPair> {
        return index.map { (definition, _) -> definition.tokensPair }.distinct()
    }

    fun getAllExchangesWithPairs(): Map<String, List<TokensPair>> {
        return index
                .map { (definition, _) -> definition.exchange to getAllPairsForExchange(definition.exchange) }
                .toMap()
    }

    private fun getAllPairsForExchange(exchange: String): List<TokensPair> {
        return index
                .filter { (definition, _) -> definition.exchange == exchange }
                .map { (definition, _) -> definition.tokensPair }
    }
}