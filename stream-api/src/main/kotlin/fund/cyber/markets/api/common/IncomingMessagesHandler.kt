package fund.cyber.markets.api.common

import com.fasterxml.jackson.databind.ObjectMapper
import fund.cyber.markets.api.common.IncomingMessageGetTopicType.*
import fund.cyber.markets.api.configuration.AppContext
import fund.cyber.markets.api.trades.TradesBroadcastersIndex
import fund.cyber.markets.model.Trade
import io.undertow.websockets.core.AbstractReceiveListener
import io.undertow.websockets.core.BufferedTextMessage
import io.undertow.websockets.core.StreamSourceFrameChannel
import io.undertow.websockets.core.WebSocketChannel
import io.undertow.websockets.core.WebSockets
import java.util.LinkedList

class IncomingMessagesHandler(
        private val tradesBroadcastersIndex: TradesBroadcastersIndex,
        private val jsonSerializer: ObjectMapper = AppContext.jsonSerializer
) : AbstractReceiveListener() {

    private val commandsParser = WebSocketCommandsParser()


    override fun onFullTextMessage(wsChannel: WebSocketChannel, bufferedMessage: BufferedTextMessage) {
        val command = commandsParser.parseMessage(bufferedMessage.data)
        when (command) {
            is UnknownCommand -> {
            }
            is TradeChannelInfoCommand -> {
                when (command.type) {
                    PAIRS ->
                        WebSockets.sendText(
                                jsonSerializer.writeValueAsString(tradesBroadcastersIndex.getAllPairs()),
                                wsChannel, null
                        )
                    EXCHANGES ->
                        WebSockets.sendText(
                                jsonSerializer.writeValueAsString(tradesBroadcastersIndex.getAllExchangesWithPairs()),
                                wsChannel, null
                        )
                }
            }
            is TradeChannelSubscriptionCommand -> {
                val broadcasters = tradesBroadcastersIndex.broadcastersFor(command.pairs, command.exchanges)
                val result: LinkedList<Trade> = LinkedList()
                var attemptCounter = 0
                while (result.size < 10 && attemptCounter < 30) {
                    val randomTrade = broadcasters.elementAt(Int.rand(0, broadcasters.size))
                            .getRandomTradeFromBroadcaster()
                    if (randomTrade != null) {
                        val unique = result.none { it.tradeId == randomTrade.tradeId }
                        if (unique) {
                            result.add(randomTrade)
                        }
                    }
                    attemptCounter++
                }
                WebSockets.sendText(jsonSerializer.writeValueAsString(result), wsChannel, null)
                broadcasters.forEach { broadcaster -> broadcaster.registerChannel(wsChannel) }
            }
        }
    }

    override fun onClose(webSocketChannel: WebSocketChannel, channel: StreamSourceFrameChannel) {
        super.onClose(webSocketChannel, channel)
    }

}

fun Int.Companion.rand(from: Int, to: Int) = (Math.random() * (to - from) + from).toInt()