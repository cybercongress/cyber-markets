package fund.cyber.markets.api.common

import fund.cyber.markets.model.TokensPairInitializer
import kotlinx.coroutines.experimental.channels.Channel


interface ChannelsIndexUpdateListener<T> {
    fun newChannel(exchange: String, pairInitializer: TokensPairInitializer, channel: Channel<T>)
}


class ChannelsIndex<T> {

    private val index: MutableMap<String, MutableMap<TokensPairInitializer, Channel<T>>> = HashMap()
    private val listeners = ArrayList<ChannelsIndexUpdateListener<T>>()


    /**
     * Returns a channel for given <exchange, pairInitializer>
     * If channel doesn't exists, create new one and notify listeners
     */
    fun channelFor(exchange: String, pairInitializer: TokensPairInitializer): Channel<T> {

        val pairsIndex = index[exchange]

        if (pairsIndex != null) {
            val channel = pairsIndex[pairInitializer]
            if (channel != null) {
                return channel
            }
        }

        val newChannel = Channel<T>()
        if (pairsIndex != null) {
            pairsIndex.put(pairInitializer, newChannel)
        } else {
            val newPairsIndex = HashMap<TokensPairInitializer, Channel<T>>()
            newPairsIndex.put(pairInitializer, newChannel)
            index.put(exchange, newPairsIndex)
        }

        listeners.forEach { listener -> listener.newChannel(exchange, pairInitializer, newChannel) }
        return newChannel
    }

    fun addChannelsListener(listener: ChannelsIndexUpdateListener<T>) = listeners.add(listener)
}