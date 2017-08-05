package fund.cyber.markets.api.common

import fund.cyber.markets.api.trades.TradesBroadcastersIndex
import io.undertow.websockets.core.AbstractReceiveListener
import io.undertow.websockets.core.BufferedTextMessage
import io.undertow.websockets.core.StreamSourceFrameChannel
import io.undertow.websockets.core.WebSocketChannel

class IncomingMessagesHandler(
        private val tradesBroadcastersIndex: TradesBroadcastersIndex
) : AbstractReceiveListener() {

    private val commandsParser = WebSocketCommandsParser()


    override fun onFullTextMessage(wsChannel: WebSocketChannel, bufferedMessage: BufferedTextMessage) {
        val command = commandsParser.parseMessage(bufferedMessage.data)
        when (command) {
            is UnknownCommand -> {}
            is TradeChannelSubscriptionCommand -> {
                tradesBroadcastersIndex.broadcastersFor(command.pairs).forEach { broadcaster ->
                    broadcaster.registerChannel(wsChannel)
                }
            }
        }
    }

    override fun onClose(webSocketChannel: WebSocketChannel, channel: StreamSourceFrameChannel) {
        super.onClose(webSocketChannel, channel)
    }
}