package fund.cyber.markets.api.rest.configuration

import fund.cyber.markets.common.EXCHANGES_CONNECTOR_API_URLS
import fund.cyber.markets.common.EXCHANGES_CONNECTOR_API_URLS_DEFAULT
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RestApiConfiguration(
    @Value("\${$EXCHANGES_CONNECTOR_API_URLS:$EXCHANGES_CONNECTOR_API_URLS_DEFAULT}")
    val exchangesConnectorApiUrls: String
) {

    @Bean
    fun connectorApiUrls(): List<String> {
        return exchangesConnectorApiUrls.split(",").map { url -> url.trim() }
    }

}