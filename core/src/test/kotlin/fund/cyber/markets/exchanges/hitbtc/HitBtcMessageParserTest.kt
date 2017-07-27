package fund.cyber.markets.exchanges.hitbtc

import fund.cyber.markets.model.Trade
import fund.cyber.markets.model.TradeType.BUY
import fund.cyber.markets.model.TradeType.SELL
import fund.cyber.markets.webscoket.ContainingUnknownTokensPairMessage
import fund.cyber.markets.webscoket.TradesAndOrdersUpdatesMessage
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.math.BigDecimal

@DisplayName("HitBtc Message Parser: ")
class HitBtcMessageParserTest {

    val message = """
      {
        "MarketDataIncrementalRefresh":{
          "seqNo":14056,
          "symbol":"LTCBTC",
          "exchangeStatus":"working",
          "ask":[{"price":"0.00020699","size":12},{"price":"0.00021699","size":1}],
          "bid":[{"price":"0.00020699","size":3},{"price":"0.00020699","size":0}],
          "trade":[
            {
              "price":"0.00020699",
              "size":12,
              "tradeId":12987994,
              "timestamp":1500048730510,
              "side":"buy"
            },
            {
              "price":"0.00133699",
              "size":2,
              "tradeId":12987997,
              "timestamp":1500048730511,
              "side":"sell"
            }
          ],
          "timestamp":1500048731645}}
    """

    @Test
    @DisplayName("Should parse two ok trades")
    fun testParseOkTrade() {

        val tokensPair = HitBtcTokensPair(
                base = "LTC", quote = "BTC", symbol = "LTCBTC",
                lotSize = BigDecimal("0.1"), priceStep = BigDecimal("0.00001")
        )

        val channelSymbolForTokensPair = mapOf(Pair(tokensPair.symbol, tokensPair))
        val messageParser = HitBtcMessageParser(channelSymbolForTokensPair)

        val exchangeMessage = messageParser.parseMessage(message)
        Assertions.assertTrue(exchangeMessage is TradesAndOrdersUpdatesMessage)
        Assertions.assertTrue((exchangeMessage as TradesAndOrdersUpdatesMessage).trades.size == 2)

        val firstTrade = Trade(
                tradeId = "12987994", exchange = "HitBtc", type = BUY,
                baseToken = tokensPair.base, quoteToken = tokensPair.quote,
                baseAmount = BigDecimal("1.2"), quoteAmount = BigDecimal("0.000248388"),
                spotPrice = BigDecimal("0.00020699"), timestamp = 1500048731
        )
        val secondTrade = Trade(
                tradeId = "12987997", exchange = "HitBtc", type = SELL,
                baseToken = tokensPair.base, quoteToken = tokensPair.quote,
                baseAmount = BigDecimal("0.2"), quoteAmount = BigDecimal("0.000267398"),
                spotPrice = BigDecimal("0.00133699"), timestamp = 1500048731
        )
        Assertions.assertEquals(firstTrade, exchangeMessage.trades[0])
        Assertions.assertEquals(secondTrade, exchangeMessage.trades[1])
    }

    @Test
    @DisplayName("Should not parse due to containing unknown tokens pair")
    fun testParseMessageWithUnknownTokensPair() {

        val messageParser = HitBtcMessageParser(emptyMap())

        val exchangeMessage = messageParser.parseMessage(message)
        Assertions.assertTrue(exchangeMessage is ContainingUnknownTokensPairMessage)
        Assertions.assertEquals("LTCBTC", (exchangeMessage as ContainingUnknownTokensPairMessage).symbol)
    }
}