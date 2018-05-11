package fund.cyber.markets.storer.configuration

import fund.cyber.markets.common.EXCHANGES_CONNECTOR_API_URL
import fund.cyber.markets.common.EXCHANGES_CONNECTOR_API_URL_DEFAULT
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

const val ORDERBOOK_SNAPSHOT_PERIOD: Long = 10 * 60 * 1000

@Configuration
class StorerConfiguration(
    @Value("\${$EXCHANGES_CONNECTOR_API_URL:$EXCHANGES_CONNECTOR_API_URL_DEFAULT}")
    val exchangesConnectorApiUrl: String
) {

    @Bean
    fun connectorApiUrl(): String {
        return exchangesConnectorApiUrl
    }

}