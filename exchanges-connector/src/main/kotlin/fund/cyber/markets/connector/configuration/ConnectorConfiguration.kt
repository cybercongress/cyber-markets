package fund.cyber.markets.connector.configuration

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

const val EXCHANGES = "EXCHANGES"
const val EXCHANGES_DEFAULT = "bitfinex,bitstamp,gdax,hitbtc,poloniex,binance,etherdelta"

const val PARITY_URL = "PARITY_URL"
const val PARITY_URL_DEFAULT = "http://127.0.0.1:8545"

@Configuration
class ConnectorConfiguration(
        @Value("\${$EXCHANGES:$EXCHANGES_DEFAULT}")
        private val exchangesProperty: String,

        @Value("\${$PARITY_URL:$PARITY_URL_DEFAULT}")
        val parityUrl: String
) {
    val exchanges: Set<String> = exchangesProperty.split(",").map { it.trim().toUpperCase() }.toSet()
}