package fund.cyber.markets.connector

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
import fund.cyber.markets.connector.configuration.ConnectorConfiguration
import info.bitrich.xchangestream.bitfinex.BitfinexStreamingExchange
import info.bitrich.xchangestream.bitflyer.BitflyerStreamingExchange
import info.bitrich.xchangestream.bitstamp.BitstampStreamingExchange
import info.bitrich.xchangestream.gdax.GDAXStreamingExchange
import info.bitrich.xchangestream.gemini.GeminiStreamingExchange
import info.bitrich.xchangestream.hitbtc.HitbtcStreamingExchange
import info.bitrich.xchangestream.okcoin.OkCoinStreamingExchange
import info.bitrich.xchangestream.okcoin.OkExStreamingExchange
import info.bitrich.xchangestream.poloniex2.PoloniexStreamingExchange
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.retry.support.RetryTemplate
import org.springframework.stereotype.Component

@Component
class ConnectorRunner {

    @Autowired
    private lateinit var beanFactory: BeanFactory

    @Autowired
    private lateinit var applicationContext: ApplicationContext

    @Autowired
    private lateinit var configuration: ConnectorConfiguration

    @Autowired
    private lateinit var retryTemplate: RetryTemplate

    val connectors = mutableSetOf<ExchangeConnector>()

    fun start() {
        configuration.exchanges.forEach { exchangeName ->
            val connector = getExchangeConnectorBean(exchangeName)
            if (connector != null) {
                connectors.add(connector)
            }
        }

        connectors.forEach { connector ->
            retryTemplate.execute<Any, Exception> { connector.start() }
        }
    }

    fun shutdown() {
        connectors.forEach { connector ->
            connector.disconnect()
        }
    }

    private fun getExchangeConnectorBean(exchangeName: String): ExchangeConnector? {
        return when (exchangeName) {
            BITFINEX -> beanFactory.getBean(XchangeConnector::class.java, BitfinexStreamingExchange::class.java.name)
            BITFLYER -> beanFactory.getBean(XchangeConnector::class.java, BitflyerStreamingExchange::class.java.name)
            // todo: disabled until trades for binance will be implemented
            //BINANCE -> beanFactory.getBean(XchangeConnector::class.java, BinanceStreamingExchange::class.java.name)
            BITSTAMP -> beanFactory.getBean(XchangeConnector::class.java, BitstampStreamingExchange::class.java.name)
            GDAX -> beanFactory.getBean(XchangeConnector::class.java, GDAXStreamingExchange::class.java.name)
            GEMINI -> beanFactory.getBean(XchangeConnector::class.java, GeminiStreamingExchange::class.java.name)
            HITBTC -> beanFactory.getBean(XchangeConnector::class.java, HitbtcStreamingExchange::class.java.name)
            OKCOIN -> beanFactory.getBean(XchangeConnector::class.java, OkCoinStreamingExchange::class.java.name)
            OKEX -> beanFactory.getBean(XchangeConnector::class.java, OkExStreamingExchange::class.java.name)
            POLONIEX -> beanFactory.getBean(XchangeConnector::class.java, PoloniexStreamingExchange::class.java.name)
            ETHERDELTA -> applicationContext.getBean(EtherdeltaConnector::class.java)
            else -> null
        }
    }
}