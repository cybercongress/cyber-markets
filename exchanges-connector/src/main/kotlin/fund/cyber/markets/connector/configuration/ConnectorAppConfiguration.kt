package fund.cyber.markets.connector.configuration

import fund.cyber.markets.common.EXCHANGES
import fund.cyber.markets.common.EXCHANGES_DEFAULT
import fund.cyber.markets.common.PARITY_URL
import fund.cyber.markets.common.PARITY_URL_DEFAULT
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
class ConnectorConfiguration(
        @Value("\${$EXCHANGES:$EXCHANGES_DEFAULT}")
        private val exchangesProperty: String,

        @Value("\${$PARITY_URL:$PARITY_URL_DEFAULT}")
        val parityUrl: String
) {
    val exchanges: Set<String> = exchangesProperty.split(",").map { it.trim().toUpperCase() }.toSet()
}