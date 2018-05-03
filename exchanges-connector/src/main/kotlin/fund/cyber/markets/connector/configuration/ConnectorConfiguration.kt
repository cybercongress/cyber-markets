package fund.cyber.markets.connector.configuration

import fund.cyber.markets.common.EXCHANGES
import fund.cyber.markets.common.EXCHANGES_DEFAULT
import fund.cyber.markets.common.PARITY_URL
import fund.cyber.markets.common.PARITY_URL_DEFAULT
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import org.web3j.utils.Async

const val WEB3J_POLLING_INTERVAL = 5 * 1000L

@Configuration
class ConnectorConfiguration(
    @Value("\${$EXCHANGES:$EXCHANGES_DEFAULT}")
    private val exchangesProperty: String,

    @Value("\${$PARITY_URL:$PARITY_URL_DEFAULT}")
    val parityUrl: String
) {

    @Bean
    fun exchanges(): Set<String> = exchangesProperty.split(",").map { it.trim().toUpperCase() }.toSet()

    @Bean
    fun web3j(): Web3j {
        return Web3j.build(HttpService(parityUrl), WEB3J_POLLING_INTERVAL, Async.defaultExecutorService())
    }
}