package fund.cyber.markets.api.rest.configuration

import fund.cyber.markets.common.EXCHANGES_CONNECTOR_SERVICE_URL
import fund.cyber.markets.common.EXCHANGES_CONNECTOR_SERVICE_URL_DEFAULT
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
class RestApiConfiguration(
    @Value("\${$EXCHANGES_CONNECTOR_SERVICE_URL:$EXCHANGES_CONNECTOR_SERVICE_URL_DEFAULT}")
    val exchangesConnectorServiceUrl: String
)