package fund.cyber.markets.connector.orderbook

import fund.cyber.markets.connector.AbstarctXchangeConnector
import info.bitrich.xchangestream.core.ProductSubscription
import info.bitrich.xchangestream.core.StreamingExchangeFactory
import io.micrometer.core.instrument.MeterRegistry
import org.knowm.xchange.currency.CurrencyPair
import org.knowm.xchange.dto.marketdata.OrderBook
import org.springframework.kafka.core.KafkaTemplate

class XchangeOrderbookConnector : AbstarctXchangeConnector, OrderbookConnector {
    override var orderbooks: MutableMap<CurrencyPair, OrderBook> = mutableMapOf()

    private constructor()

    constructor(streamingExchangeClassName: String, kafkaTemplate: KafkaTemplate<String, Any>, meterRegistry: MeterRegistry) : this() {
        exchange = StreamingExchangeFactory.INSTANCE.createExchange(streamingExchangeClassName)
        this.kafkaTemplate = kafkaTemplate
        this.monitoring = meterRegistry

        orderbooks = mutableMapOf()
    }

    override fun buildSubscription(): ProductSubscription {
        val subscription = ProductSubscription.create()
        exchangeTokensPairs.forEach { pair ->
            subscription.addOrderbook(pair)
        }
        return subscription.build()
    }

    override fun subscribe() {
        log.info("Subscribing for orderbooks from $exchangeName exchange")

        exchangeTokensPairs.forEach { pair ->

            val orderbookSubscription = exchange.streamingMarketDataService
                .getOrderBook(pair)
                .subscribe({ orderbook ->
                    orderbooks[pair] = orderbook
                }) { throwable ->
                    log.error("Error in subscribing orderbook for $exchangeName, pair $pair", throwable)
                }
            subscriptions.add(orderbookSubscription)
        }
    }

}