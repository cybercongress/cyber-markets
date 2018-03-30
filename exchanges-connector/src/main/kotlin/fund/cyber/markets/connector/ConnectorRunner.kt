package fund.cyber.markets.connector

import fund.cyber.markets.connector.configuration.ConnectorConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component

@Component
class ConnectorRunner {

    @Autowired
    private lateinit var applicationContext: ApplicationContext

    @Autowired
    private lateinit var configuration: ConnectorConfiguration

    private val connectors = mutableSetOf<ExchangeConnector>()

    fun start() {
        configuration.exchanges.forEach { exchangeName ->
            val connector = getExchangeConnectorBean(exchangeName)
            if (connector != null) {
                connectors.add(connector)
            }
        }

        connectors.forEach { connector ->
            connector.connect()
            connector.subscribeAll()
        }
    }

    fun shutdown() {
        connectors.forEach { connector ->
            connector.disconnect()
        }
    }

    private fun getExchangeConnectorBean(exchangeName: String): ExchangeConnector? {
        return when (exchangeName) {
            "BITFINEX" -> applicationContext.getBean(BitfinexConnector::class.java)
            "BITSTAMP" -> applicationContext.getBean(BitstampConnector::class.java)
            "POLONIEX" -> applicationContext.getBean(PoloniexConnector::class.java)
            "GDAX" -> applicationContext.getBean(GdaxConnector::class.java)
            //todo: missing hitbtc and etherdelta exchanges
            //"HITBTC" -> HitBtcConnector
            //"ETHERDELTA" -> EtherdeltaConnector
            else -> null
        }
    }
}