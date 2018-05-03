package fund.cyber.markets.connector.orderbook

import fund.cyber.markets.common.MILLIS_TO_HOURS
import fund.cyber.markets.common.convert
import fund.cyber.markets.common.model.Order
import fund.cyber.markets.common.model.OrderType
import fund.cyber.markets.common.model.TokensPair
import fund.cyber.markets.connector.AbstarctXchangeConnector
import fund.cyber.markets.connector.configuration.EXCHANGE_TAG
import fund.cyber.markets.connector.configuration.ORDERBOOK_SIZE_METRIC
import fund.cyber.markets.connector.configuration.ORDER_TYPE_TAG
import fund.cyber.markets.connector.configuration.TOKENS_PAIR_TAG
import info.bitrich.xchangestream.core.ProductSubscription
import info.bitrich.xchangestream.core.StreamingExchangeFactory
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tags
import org.knowm.xchange.currency.CurrencyPair
import org.knowm.xchange.dto.marketdata.OrderBook
import org.knowm.xchange.dto.trade.LimitOrder
import org.springframework.kafka.core.KafkaTemplate
import java.util.*
import java.util.concurrent.atomic.AtomicLong

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

        val exchangeTag = Tags.of(EXCHANGE_TAG, exchangeName)
        val askOrderTag = Tags.of(ORDER_TYPE_TAG, OrderType.ASK.name)
        val bidOrderTag = Tags.of(ORDER_TYPE_TAG, OrderType.BID.name)

        exchangeTokensPairs.forEach { pair ->
            val exchangePairTag = exchangeTag.and(Tags.of(TOKENS_PAIR_TAG, pair.base.currencyCode + "_" + pair.counter.currencyCode))
            val askCountMonitor = monitoring.gauge(ORDERBOOK_SIZE_METRIC, exchangePairTag.and(askOrderTag), AtomicLong(0L))
            val bidCountMonitor = monitoring.gauge(ORDERBOOK_SIZE_METRIC, exchangePairTag.and(bidOrderTag), AtomicLong(0L))

            val orderbookSubscription = exchange.streamingMarketDataService
                .getOrderBook(pair)
                .subscribe({ orderbook ->
                    orderbooks[pair] = orderbook

                    askCountMonitor!!.set(orderbook.asks.size.toLong())
                    bidCountMonitor!!.set(orderbook.bids.size.toLong())
                }) { throwable ->
                    log.error("Error in subscribing orderbook for $exchangeName, pair $pair", throwable)
                }
            subscriptions.add(orderbookSubscription)
        }
    }

    override fun getOrderBookSnapshot(pair: TokensPair): fund.cyber.markets.common.model.OrderBook? {
        val orderBook = orderbooks[CurrencyPair(pair.base, pair.quote)] ?: return null
        val timestamp: Long = orderBook.timeStamp?.time ?: Date().time

        val asks = mutableListOf<Order>()
        val bids = mutableListOf<Order>()

        orderBook.asks.forEach { xchangeOrder ->
            asks.add(convertOrder(xchangeOrder, pair, OrderType.ASK, timestamp))
        }

        orderBook.bids.forEach { xchangeOrder ->
            asks.add(convertOrder(xchangeOrder, pair, OrderType.BID, timestamp))
        }

        return fund.cyber.markets.common.model.OrderBook(asks, bids, timestamp)
    }

    private fun convertOrder(xchangeOrder: LimitOrder, pair: TokensPair, type: OrderType, orderBookTimestamp: Long): Order {
        val timestamp: Long = xchangeOrder.timestamp?.time ?: orderBookTimestamp

        return Order(exchangeName,
            pair,
            type,
            timestamp,
            timestamp convert MILLIS_TO_HOURS,
            xchangeOrder.id,
            xchangeOrder.originalAmount,
            xchangeOrder.limitPrice)
    }

}