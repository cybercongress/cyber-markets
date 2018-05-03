package fund.cyber.markets.connector

import fund.cyber.markets.common.model.Exchanges.BINANCE
import fund.cyber.markets.common.model.Exchanges.BITFINEX
import fund.cyber.markets.common.model.Exchanges.BITFLYER
import fund.cyber.markets.common.model.Exchanges.BITSTAMP
import fund.cyber.markets.common.model.Exchanges.ETHERDELTA
import fund.cyber.markets.common.model.Exchanges.GDAX
import fund.cyber.markets.common.model.Exchanges.GEMINI
import fund.cyber.markets.common.model.Exchanges.HITBTC
import fund.cyber.markets.common.model.Exchanges.OKCOIN
import fund.cyber.markets.common.model.Exchanges.OKEX
import fund.cyber.markets.common.model.Exchanges.POLONIEX
import fund.cyber.markets.connector.orderbook.OrderbookConnector
import fund.cyber.markets.connector.orderbook.XchangeOrderbookConnector
import fund.cyber.markets.connector.trade.EtherdeltaTradeConnector
import fund.cyber.markets.connector.trade.XchangeTradeConnector
import info.bitrich.xchangestream.binance.BinanceStreamingExchange
import info.bitrich.xchangestream.bitfinex.BitfinexStreamingExchange
import info.bitrich.xchangestream.bitflyer.BitflyerStreamingExchange
import info.bitrich.xchangestream.bitstamp.BitstampStreamingExchange
import info.bitrich.xchangestream.gdax.GDAXStreamingExchange
import info.bitrich.xchangestream.gemini.GeminiStreamingExchange
import info.bitrich.xchangestream.hitbtc.HitbtcStreamingExchange
import info.bitrich.xchangestream.okcoin.OkCoinStreamingExchange
import info.bitrich.xchangestream.okcoin.OkExStreamingExchange
import info.bitrich.xchangestream.poloniex2.PoloniexStreamingExchange
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.support.GenericApplicationContext
import org.springframework.retry.support.RetryTemplate
import org.springframework.stereotype.Component

val XCHANGE_TRADE_CONNECTOR_CLASS = XchangeTradeConnector::class.java
val XCHANGE_ORDERBOOK_CONNECTOR_CLASS = XchangeOrderbookConnector::class.java

@Component
class ConnectorRunner {
    val log = LoggerFactory.getLogger(javaClass)!!

    @Autowired
    private lateinit var applicationContext: GenericApplicationContext

    @Autowired
    private lateinit var exchanges: Set<String>

    @Autowired
    private lateinit var retryTemplate: RetryTemplate

    val tradesConnectors = mutableMapOf<String, Connector>()
    val orderbookConnectors = mutableMapOf<String, OrderbookConnector>()

    fun start() {
        exchanges.forEach { exchangeName ->
            addConnectorBean(exchangeName)
        }

        tradesConnectors.forEach { entry ->
            retryTemplate.execute<Any, Exception> { entry.value.start() }
        }
        orderbookConnectors.forEach { entry ->
            retryTemplate.execute<Any, Exception> { entry.value.start() }
        }
    }

    fun shutdown() {
        tradesConnectors.forEach { entry ->
            entry.value.disconnect()
        }
        orderbookConnectors.forEach { entry ->
            entry.value.disconnect()
        }
    }

    private fun addConnectorBean(exchangeName: String) {

        var tradeConnector: Connector? = null
        var orderbookConnector: OrderbookConnector? = null
        val beanFactory = applicationContext.beanFactory

        when (exchangeName) {
            BITFINEX -> {
                tradeConnector = beanFactory.getBean(XCHANGE_TRADE_CONNECTOR_CLASS, BitfinexStreamingExchange::class.java.name)
                orderbookConnector = beanFactory.getBean(XCHANGE_ORDERBOOK_CONNECTOR_CLASS, BitfinexStreamingExchange::class.java.name)
            }
            BITFLYER -> {
                tradeConnector = beanFactory.getBean(XCHANGE_TRADE_CONNECTOR_CLASS, BitflyerStreamingExchange::class.java.name)
                orderbookConnector = beanFactory.getBean(XCHANGE_ORDERBOOK_CONNECTOR_CLASS, BitflyerStreamingExchange::class.java.name)
            }
            BINANCE -> {
                tradeConnector = beanFactory.getBean(XCHANGE_TRADE_CONNECTOR_CLASS, BinanceStreamingExchange::class.java.name)
                orderbookConnector = beanFactory.getBean(XCHANGE_ORDERBOOK_CONNECTOR_CLASS, BinanceStreamingExchange::class.java.name)
            }
            BITSTAMP -> {
                tradeConnector = beanFactory.getBean(XCHANGE_TRADE_CONNECTOR_CLASS, BitstampStreamingExchange::class.java.name)
                orderbookConnector = beanFactory.getBean(XCHANGE_ORDERBOOK_CONNECTOR_CLASS, BitstampStreamingExchange::class.java.name)
            }
            GDAX -> {
                tradeConnector = beanFactory.getBean(XCHANGE_TRADE_CONNECTOR_CLASS, GDAXStreamingExchange::class.java.name)
                orderbookConnector = beanFactory.getBean(XCHANGE_ORDERBOOK_CONNECTOR_CLASS, GDAXStreamingExchange::class.java.name)
            }
            GEMINI -> {
                tradeConnector = beanFactory.getBean(XCHANGE_TRADE_CONNECTOR_CLASS, GeminiStreamingExchange::class.java.name)
                orderbookConnector = beanFactory.getBean(XCHANGE_ORDERBOOK_CONNECTOR_CLASS, GeminiStreamingExchange::class.java.name)
            }
            HITBTC -> {
                tradeConnector = beanFactory.getBean(XCHANGE_TRADE_CONNECTOR_CLASS, HitbtcStreamingExchange::class.java.name)
                orderbookConnector = beanFactory.getBean(XCHANGE_ORDERBOOK_CONNECTOR_CLASS, HitbtcStreamingExchange::class.java.name)
            }
            OKCOIN -> {
                tradeConnector = beanFactory.getBean(XCHANGE_TRADE_CONNECTOR_CLASS, OkCoinStreamingExchange::class.java.name)
                orderbookConnector = beanFactory.getBean(XCHANGE_ORDERBOOK_CONNECTOR_CLASS, OkCoinStreamingExchange::class.java.name)
            }
            OKEX -> {
                tradeConnector = beanFactory.getBean(XCHANGE_TRADE_CONNECTOR_CLASS, OkExStreamingExchange::class.java.name)
                orderbookConnector = beanFactory.getBean(XCHANGE_ORDERBOOK_CONNECTOR_CLASS, OkExStreamingExchange::class.java.name)
            }
            POLONIEX -> {
                tradeConnector = beanFactory.getBean(XCHANGE_TRADE_CONNECTOR_CLASS, PoloniexStreamingExchange::class.java.name)
                orderbookConnector = beanFactory.getBean(XCHANGE_ORDERBOOK_CONNECTOR_CLASS, PoloniexStreamingExchange::class.java.name)
            }
            ETHERDELTA -> {
                tradeConnector = applicationContext.getBean(EtherdeltaTradeConnector::class.java)
            }
            else -> {
                log.info("Unknown exchange with name $exchangeName")
            }
        }

        if (tradeConnector != null) {
            tradesConnectors[exchangeName] = tradeConnector
        }
        if (orderbookConnector != null) {
            orderbookConnectors[exchangeName] = orderbookConnector
        }
    }
}