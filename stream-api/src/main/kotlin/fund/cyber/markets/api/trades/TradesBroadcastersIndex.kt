package fund.cyber.markets.api.trades

import fund.cyber.markets.model.TokensPair
import java.util.LinkedList


class TradesBroadcastersIndex : TradesChannelsIndexUpdateListener {

    private val index: MutableMap<TokensPair, MutableMap<String, TokensPairTradesBroadcaster>> = HashMap()

    override fun newTradesChannel(exchange: String, pair: TokensPair, channel: TradesChannel) {
        val broadcaster = TokensPairTradesBroadcaster(channel)
        index.getOrPut(pair, { HashMap() }).put(exchange, broadcaster)
    }

    fun broadcastersFor(pairs: List<TokensPair>, exchanges: List<String> ): Collection<TokensPairTradesBroadcaster> {
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

    fun getAllPairs (): Collection<TokensPair>{
        return index.keys
    }

    fun getAllExchangesWithPairs (): MutableMap<String, List<TokensPair>>{
        var resultMap: HashMap<String, List<TokensPair>> = hashMapOf()
        val indexCopy = index
        val flatMap = indexCopy.flatMap { (_, pairIndex) -> pairIndex.entries }
        flatMap.forEach { exc ->resultMap.put(exc.key, getAllPairsForExchange(exc.key))}
        return resultMap
    }

    private fun getAllPairsForExchange (exchange: String): MutableList<TokensPair> {
        val indexCopy = index
        var resultPairsList : LinkedList<TokensPair> = LinkedList()
        for (el in indexCopy) {
            if(el.value.keys.contains(exchange)){
                resultPairsList.add(el.key)
            }
        }
        return resultPairsList
    }
}