package fund.cyber.markets.api.common

import fund.cyber.markets.api.orders.OrdersBroadcaster
import fund.cyber.markets.api.tickers.TickersBroadcaster
import fund.cyber.markets.api.trades.TradesBroadcaster
import fund.cyber.markets.model.Order
import fund.cyber.markets.model.TokensPairInitializer
import fund.cyber.markets.model.Trade
import fund.cyber.markets.tickers.model.Ticker
import kotlinx.coroutines.experimental.channels.Channel

class TradesBroadcastersIndex : BroadcastersIndex<Trade, TradesBroadcaster>() {
    override fun newChannel(exchange: String, pairInitializer: TokensPairInitializer, windowDuration: Long, channel: Channel<Trade>) {
        val broadcaster = TradesBroadcaster(channel)
        index.add(BroadcasterDefinition(pairInitializer, exchange, windowDuration, broadcaster))
    }
}

class OrdersBroadcastersIndex : BroadcastersIndex<List<Order>, OrdersBroadcaster>() {
    override fun newChannel(exchange: String, pairInitializer: TokensPairInitializer, windowDuration: Long, channel: Channel<List<Order>>) {
        val broadcaster = OrdersBroadcaster(channel)
        index.add(BroadcasterDefinition(pairInitializer, exchange, windowDuration, broadcaster))
    }
}

class TickersBroadcastersIndex : BroadcastersIndex<Ticker, TickersBroadcaster>() {
    override fun newChannel(exchange: String, pairInitializer: TokensPairInitializer, windowDuration: Long, channel: Channel<Ticker>) {
        val broadcaster = TickersBroadcaster(channel)
        index.add(BroadcasterDefinition(pairInitializer, exchange, windowDuration, broadcaster))
    }
}

data class BroadcasterDefinition(val tokensPair: TokensPairInitializer,
                                 val exchange: String,
                                 val windowDuration: Long,
                                 val broadcaster: Broadcaster)

abstract class BroadcastersIndex<T, B : Broadcaster> : ChannelsIndexUpdateListener<T> {

    //protected val index: MutableMap<BroadcasterDefinition, B> = ConcurrentHashMap()
    protected val index: MutableCollection<BroadcasterDefinition> = mutableListOf() //todo: use thread safe collection

    fun broadcastersFor(pairInitializers: List<TokensPairInitializer>, exchanges: List<String>, windowDurations: List<Long> = emptyList()): Collection<B> {

        var broadcasters : Collection<BroadcasterDefinition> = ArrayList(index)
        when {
            !pairInitializers.isEmpty() -> broadcasters = broadcasters.filter {
                (tokensPair) -> pairInitializers.contains(tokensPair)
            }
            !exchanges.isEmpty() -> broadcasters = broadcasters.filter {
                definition -> exchanges.contains(definition.exchange)
            }
            !windowDurations.isEmpty() -> broadcasters = broadcasters.filter {
                definition -> definition.windowDuration < 0 || windowDurations.contains(definition.windowDuration)
            }
        }

        return broadcasters.map { definition -> definition.broadcaster as B }
    }

    fun getAllPairs(): Collection<TokensPairInitializer> {
        return index.map { (tokensPair) -> tokensPair }
    }

    fun getAllExchangesWithPairs(): Map<String, List<TokensPairInitializer>> {
        return index
                .map { definition -> definition.exchange to getAllPairsForExchange(definition.exchange) }
                .toMap()
    }

    private fun getAllPairsForExchange(exchange: String): List<TokensPairInitializer> {
        return index
                .filter { definition -> definition.exchange == exchange }
                .map { (tokensPair) -> tokensPair }
    }
}