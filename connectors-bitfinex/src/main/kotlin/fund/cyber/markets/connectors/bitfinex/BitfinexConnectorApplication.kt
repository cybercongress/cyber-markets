package fund.cyber.markets.connectors.bitfinex

import fund.cyber.markets.connectors.bitfinex.connector.BitfinexOrdersEndpoint
import fund.cyber.markets.connectors.bitfinex.connector.BitfinexTradesEndpoint
import fund.cyber.markets.connectors.common.kafka.ConnectorKafkaProducer
import fund.cyber.markets.connectors.common.kafka.OrdersUpdateProducerRecord
import fund.cyber.markets.connectors.common.kafka.TradeProducerRecord
import fund.cyber.markets.connectors.helpers.concurrent
import fund.cyber.markets.model.OrdersBatch
import fund.cyber.markets.model.Trade

/*----------------------------- Undertow ------------------------------------------*/
/*----------------------------- Others ---------------------------------------------*/
val supportedTradesEndpoints = listOf(
        BitfinexTradesEndpoint()
)
val supportedOrdersEndpoints = listOf(
        BitfinexOrdersEndpoint()
)
val tradeKafkaProducer = ConnectorKafkaProducer<Trade>()
val orderKafkaProducer = ConnectorKafkaProducer<OrdersBatch>()
fun main(args: Array<String>) {
    val debugMode = System.getProperty("debug") != null

    supportedTradesEndpoints.forEach { exchange ->
        concurrent {
            val dataChannel = exchange.subscribe()
            concurrent {
                while (true) {
                    val message = dataChannel.receive()
                    message.trades.forEach { trade ->
                        if (debugMode) println(trade) else tradeKafkaProducer.send(TradeProducerRecord(trade))
                    }
                }
            }
        }
    }

    supportedOrdersEndpoints.forEach { exchange ->
        concurrent {
            val dataChannel = exchange.subscribe()
            concurrent {
                while (true) {
                    val message = dataChannel.receive()
                    if (debugMode) println(message) else orderKafkaProducer.send(OrdersUpdateProducerRecord(message.ordersBatch))
                }
            }
        }
    }
}