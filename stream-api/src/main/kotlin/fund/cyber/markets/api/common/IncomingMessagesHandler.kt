package fund.cyber.markets.api.common

import com.fasterxml.jackson.databind.ObjectMapper
import fund.cyber.markets.api.configuration.AppContext
import fund.cyber.markets.api.trades.TokensPairTradesBroadcaster
import fund.cyber.markets.api.trades.TradesBroadcastersIndex
import io.undertow.websockets.core.AbstractReceiveListener
import io.undertow.websockets.core.BufferedTextMessage
import io.undertow.websockets.core.StreamSourceFrameChannel
import io.undertow.websockets.core.WebSocketChannel
import io.undertow.websockets.core.WebSockets

class IncomingMessagesHandler(
        private val tradesBroadcastersIndex: TradesBroadcastersIndex,
        private val jsonSerializer: ObjectMapper = AppContext.jsonSerializer
) : AbstractReceiveListener() {

    private val commandsParser = WebSocketCommandsParser()


    override fun onFullTextMessage(wsChannel: WebSocketChannel, bufferedMessage: BufferedTextMessage) {
        val command = commandsParser.parseMessage(bufferedMessage.data)
        when (command) {
            is UnknownCommand -> {}
            is TradeChannelInfoCommand -> {
                when (command.type) {
                    IncomingMessageGetTopicType.PAIRS ->
                        WebSockets.sendText(
                                jsonSerializer.writeValueAsString(
                                        tradesBroadcastersIndex.broadcastersGetAllPairs()),
                                wsChannel,null)
                    IncomingMessageGetTopicType.EXCHANGES ->
                        WebSockets.sendText(
                                jsonSerializer.writeValueAsString(
                                        tradesBroadcastersIndex.broadcastersGetExchangesTree()),
                                wsChannel, null)
                }
            }
            is TradeChannelSubscriptionCommand -> {
                filterSubscriptionBroadcast(command)?.forEach { broadcaster ->
                    broadcaster.registerChannel(wsChannel)
                }
            }
        }
    }

    override fun onClose(webSocketChannel: WebSocketChannel, channel: StreamSourceFrameChannel) {
        super.onClose(webSocketChannel, channel)
    }

    private fun filterSubscriptionBroadcast(command: TradeChannelSubscriptionCommand) : Collection<TokensPairTradesBroadcaster> {
        return when {
            command.pairs!=null && command.exchanges!=null -> tradesBroadcastersIndex.broadcastersFor(command.pairs, command.exchanges)
            command.pairs!= null -> tradesBroadcastersIndex.broadcastersForPairs(command.pairs)
            command.exchanges!=null -> tradesBroadcastersIndex.broadcastersForExchanges(command.exchanges)
            else -> tradesBroadcastersIndex.broadcastersForAll()
        }
    }
}