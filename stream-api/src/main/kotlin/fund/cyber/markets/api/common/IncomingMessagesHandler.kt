package fund.cyber.markets.api.common

import com.fasterxml.jackson.databind.ObjectMapper
import fund.cyber.markets.api.common.IncomingMessageGetTopicType.*
import fund.cyber.markets.api.configuration.AppContext
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
                    PAIRS ->
                        WebSockets.sendText(
                                jsonSerializer.writeValueAsString(tradesBroadcastersIndex.getAllPairs()),
                                wsChannel,null
                        )
                    EXCHANGES ->
                        WebSockets.sendText(
                                jsonSerializer.writeValueAsString(tradesBroadcastersIndex.getAllExchangesWithPairs()),
                                wsChannel, null
                        )
                }
            }
            is TradeChannelSubscriptionCommand -> {
                tradesBroadcastersIndex.broadcastersFor(command.pairs, command.exchanges)
                        ?.forEach { broadcaster -> broadcaster.registerChannel(wsChannel)
                }
            }
        }
    }

    override fun onClose(webSocketChannel: WebSocketChannel, channel: StreamSourceFrameChannel) {
        super.onClose(webSocketChannel, channel)
    }

}