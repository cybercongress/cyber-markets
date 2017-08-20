package fund.cyber.markets.connectors.common.kafka

import fund.cyber.markets.connectors.common.OrdersUpdatesMessage
import fund.cyber.markets.model.Order
import fund.cyber.markets.model.Trade
import org.apache.kafka.clients.producer.ProducerRecord


class TradeProducerRecord(trade: Trade): ProducerRecord<String, Trade>("TRADES-${trade.exchange}", trade)

class OrdersUpdateProducerRecord(ordersUpdatesMessage: OrdersUpdatesMessage):
        ProducerRecord<String, OrdersUpdatesMessage>("ORDERS-${ordersUpdatesMessage.exchange}", ordersUpdatesMessage)