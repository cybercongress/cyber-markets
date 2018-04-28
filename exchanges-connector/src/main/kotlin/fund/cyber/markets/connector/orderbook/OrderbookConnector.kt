package fund.cyber.markets.connector.orderbook

import fund.cyber.markets.common.model.TokensPair
import fund.cyber.markets.connector.Connector
import org.knowm.xchange.currency.CurrencyPair
import org.knowm.xchange.dto.marketdata.OrderBook

interface OrderbookConnector : Connector {
    var orderbooks: MutableMap<CurrencyPair, OrderBook>
    fun getOrderBookSnapshot(pair: TokensPair): fund.cyber.markets.common.model.OrderBook
}