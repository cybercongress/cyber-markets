package fund.cyber.markets.api.trades

import fund.cyber.markets.model.TokensPair


class TradesBroadcastersIndex : TradesChannelsIndexUpdateListener {

    private val index: MutableMap<TokensPair, MutableMap<String, TokensPairTradesBroadcaster>> = HashMap()

    override fun newTradesChannel(exchange: String, pair: TokensPair, channel: TradesChannel) {
        val broadcaster = TokensPairTradesBroadcaster(channel)
        index.getOrPut(pair, { HashMap() }).put(exchange, broadcaster)
    }

    fun broadcastersFor(pairs: List<TokensPair>): Collection<TokensPairTradesBroadcaster> {
        return index
                .filter { (pair, _) -> pairs.contains(pair) }
                .flatMap { (_, pairIndex) -> pairIndex.values }
    }
}