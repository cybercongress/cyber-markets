package fund.cyber.markets.connectors.bitfinex

import fund.cyber.markets.connectors.common.ExchangeMessage
import fund.cyber.markets.connectors.common.ExchangeType
import fund.cyber.markets.connectors.common.ws.*
import fund.cyber.markets.model.TokensPair
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

class BitfinexExchange(type: ExchangeType) : WsCommonExchange(type) {
    private val LOGGER = LoggerFactory.getLogger(BitfinexExchange::class.java)!!


    override val name = "Bitfinex"
    override val wsAddress = "wss://api.bitfinex.com/ws/2"
    override val connector: WsConnector = ReconnectableWsConnector(wsAddress, name)
    override val reconnectable: Boolean
        get() = type == ExchangeType.ORDERS

    private val channelSymbolForTokensPair = HashMap<String, TokensPair>()
    private val tradesChannelIdForTokensPair = ConcurrentHashMap<Int, TokensPair>(64, 0.75f, 5)
    override val messageParser = BitfinexMessageParser(channelSymbolForTokensPair, tradesChannelIdForTokensPair)

    override suspend fun updatePairs(): List<String> = emptyList()

    suspend override fun initMetadata() {
        channelSymbolForTokensPair.put("tETHBTC", TokensPair("ETH", "BTC"))
    }

    override fun getSubscribeMessage(pairSymbol: String): String {
        return when(type) {
            ExchangeType.ORDERS -> """{"event":"subscribe","channel":"book","symbol":"$pairSymbol"}"""
            ExchangeType.TRADES -> """{"event":"subscribe","channel":"trades","symbol":"$pairSymbol"}"""
        }
    }

    override fun getPairsToSubscribe(): Collection<String> {
        return channelSymbolForTokensPair.keys
    }

    override fun handleUnknownMessage(message: ExchangeMessage) {
        super.handleUnknownMessage(message)
        if (message is TradeChannelSubscribed) {
            tradesChannelIdForTokensPair.put(message.channelId, message.tokensPair)
            LOGGER.debug("Subscribed to Bitfinex ${message.tokensPair.label()} channel")
        }
    }
}