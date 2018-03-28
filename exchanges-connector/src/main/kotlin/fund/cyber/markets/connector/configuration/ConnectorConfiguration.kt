package fund.cyber.markets.connector.configuration

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

const val EXCHANGES_PROP = "EXCHANGES"
const val EXCHANGES_PROP_DEFAULT = "bitfinex,bitstamp,gdax,hitbtc,poloniex"

@Configuration
class ConnectorConfiguration(
        @Value("\${$EXCHANGES_PROP:$EXCHANGES_PROP_DEFAULT}")
        private val exchangesProperty: String
) {
    val exchanges: Set<String> = exchangesProperty
            .split(",")
            .map { exchangeName ->
                exchangeName.trim().toUpperCase()
            }
            .toSet()
}