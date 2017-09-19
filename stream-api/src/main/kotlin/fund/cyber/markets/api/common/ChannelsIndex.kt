package fund.cyber.markets.api.common

import fund.cyber.markets.model.TokensPair
import kotlinx.coroutines.experimental.channels.Channel


interface ChannelsIndexUpdateListener<T> {
    fun newChannel(exchange: String, pair: TokensPair, channel: Channel<T>)
}


class ChannelsIndex<T> {

    private val index: MutableMap<String, MutableMap<TokensPair, Channel<T>>> = HashMap()
    private val listeners = ArrayList<ChannelsIndexUpdateListener<T>>()


    /**
     * Returns a channel for given <exchange, pair>
     * If channel doesn't exists, create new one and notify listeners
     */
    fun channelFor(exchange: String, pair: TokensPair): Channel<T> {

        val pairsIndex = index[exchange]

        if (pairsIndex != null) {
            val channel = pairsIndex[pair]
            if (channel != null) {
                return channel
            }
        }

        val newChannel = Channel<T>()
        if (pairsIndex != null) {
            pairsIndex.put(pair, newChannel)
        } else {
            val newPairsIndex = HashMap<TokensPair, Channel<T>>()
            newPairsIndex.put(pair, newChannel)
            index.put(exchange, newPairsIndex)
        }

        listeners.forEach { listener -> listener.newChannel(exchange, pair, newChannel) }
        return newChannel
    }

    fun addChannelsListener(listener: ChannelsIndexUpdateListener<T>) = listeners.add(listener)
}