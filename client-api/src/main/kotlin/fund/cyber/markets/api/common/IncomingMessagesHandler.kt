package fund.cyber.markets.api.common

import io.undertow.websockets.core.AbstractReceiveListener
import io.undertow.websockets.core.BufferedTextMessage
import io.undertow.websockets.core.StreamSourceFrameChannel
import io.undertow.websockets.core.WebSocketChannel

class IncomingMessagesHandler : AbstractReceiveListener() {

    private val commandsParser = WebSocketCommandsParser()


    override fun onFullTextMessage(wsChannel: WebSocketChannel, bufferedMessage: BufferedTextMessage) {
        val message = bufferedMessage.data
        when (commandsParser.parseMessage(message)) {
            is UnknownMessage -> {}
            is TradeChannelSubscriptionCommand -> {}
        }
    }

    override fun onClose(webSocketChannel: WebSocketChannel, channel: StreamSourceFrameChannel) {
        super.onClose(webSocketChannel, channel)
    }
}