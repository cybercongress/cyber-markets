package fund.cyber.markets.api.common

import fund.cyber.markets.dto.TokensPair
import fund.cyber.markets.model.TokensPairInitializer
import kotlinx.coroutines.experimental.channels.Channel


interface ChannelsIndexUpdateListener<T> {
    fun newChannel(exchange: String, pairInitializer: TokensPair, windowDuration: Long, channel: Channel<T>)
}


class ChannelsIndex<T> {

    data class ChannelDefinition(
            val exchange: String,
            val pairInitializer: TokensPairInitializer,
            val windowDuration: Long,
            val channel: Channel<*>
    )

    private val index: MutableList<ChannelDefinition> = mutableListOf()
    private val listeners = ArrayList<ChannelsIndexUpdateListener<T>>()


    /**
     * Returns a channel for given <exchange, pairInitializer, windowDuration>
     * If channel doesn't exists, create new one and notify listeners
     */
    fun channelFor(exchange: String, pairInitializer: TokensPairInitializer, windowDuration: Long = -1L): Channel<T> {

        val channelDef = index.find {
            definition -> definition.exchange == exchange
            && definition.pairInitializer == pairInitializer
            && (windowDuration < 0 || definition.windowDuration == windowDuration)
        }

        if (channelDef?.channel != null) {
            return channelDef.channel as Channel<T>
        }

        val newChannel = Channel<T>()
        index.add(ChannelDefinition(exchange, pairInitializer, windowDuration, newChannel))

        listeners.forEach { listener -> listener.newChannel(exchange, pairInitializer, windowDuration, newChannel) }
        return newChannel
    }

    fun addChannelsListener(listener: ChannelsIndexUpdateListener<T>) = listeners.add(listener)
}