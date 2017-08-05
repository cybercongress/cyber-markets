package fund.cyber.markets.api.trades

import fund.cyber.markets.model.TokensPair
import fund.cyber.markets.model.Trade
import kotlinx.coroutines.experimental.channels.Channel


interface TradesChannelsIndexUpdateListener {
    fun newTradesChannel(exchange: String, pair: TokensPair, channel: TradesChannel)
}


class TradesChannelsIndex {

    private val index: MutableMap<String, MutableMap<TokensPair, TradesChannel>> = HashMap()
    private val listeners = ArrayList<TradesChannelsIndexUpdateListener>()


    /**
     * Returns a channel for given <exchange, pair>
     * If channel doesn't exists, create new one and notify listeners
     */
    fun channelFor(exchange: String, pair: TokensPair): TradesChannel {

        val pairsIndex = index[exchange]

        if (pairsIndex != null) {
            val channel = pairsIndex[pair]
            if (channel != null) {
                return channel
            }
        }

        val newChannel = Channel<Trade>()
        if (pairsIndex != null) {
            pairsIndex.put(pair, newChannel)
        } else {
            val newPairsIndex = HashMap<TokensPair, TradesChannel>()
            newPairsIndex.put(pair, newChannel)
            index.put(exchange, newPairsIndex)
        }

        listeners.forEach { listener -> listener.newTradesChannel(exchange, pair, newChannel) }
        return newChannel
    }

    fun addTradesChannelsListener(listener: TradesChannelsIndexUpdateListener) = listeners.add(listener)
}