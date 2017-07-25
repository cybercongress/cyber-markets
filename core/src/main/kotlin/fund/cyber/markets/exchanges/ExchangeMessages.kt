package fund.cyber.markets.webscoket

import fund.cyber.markets.model.Trade


/**
 * Represents parsed message from exchange.
 */
open class ExchangeMessage

open class NotParsedExchangeMessage : ExchangeMessage()

/**
 * Represents unknown format message obtained from exchange.
 */
open class UnknownFormatMessage(
        val message: String
) : NotParsedExchangeMessage()

/**
 * Represents right structured message with unknown tokens pair.
 * Symbol -> either channelId or channelSymbol
 */
open class ContainingUnknownTokensPairMessage(
        val symbol: String
) : NotParsedExchangeMessage()

/**
 * Represents trades and orders updates received from exchange.
 */
data class TradesAndOrdersUpdatesMessage(
        val trades: List<Trade> = ArrayList()
) : ExchangeMessage()


