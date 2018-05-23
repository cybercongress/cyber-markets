package fund.cyber.markets.storer.configuration

import fund.cyber.markets.common.EXCHANGES_CONNECTOR_API_URLS
import fund.cyber.markets.common.EXCHANGES_CONNECTOR_API_URLS_DEFAULT
import fund.cyber.markets.common.MINUTES_TO_MILLIS
import fund.cyber.markets.common.ORDERBOOK_SNAPSHOT_PERIOD_MIN
import fund.cyber.markets.common.ORDERBOOK_SNAPSHOT_PERIOD_MIN_DEFAULT
import fund.cyber.markets.common.SAVE_ORDERBOOKS
import fund.cyber.markets.common.SAVE_ORDERBOOKS_DEFAULT
import fund.cyber.markets.common.convert
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class StorerConfiguration(
    @Value("\${$EXCHANGES_CONNECTOR_API_URLS:$EXCHANGES_CONNECTOR_API_URLS_DEFAULT}")
    val exchangesConnectorApiUrls: String,

    @Value("\${$ORDERBOOK_SNAPSHOT_PERIOD_MIN:$ORDERBOOK_SNAPSHOT_PERIOD_MIN_DEFAULT}")
    val orderBookSnapshotPeriod: Long,

    @Value("\${$SAVE_ORDERBOOKS:$SAVE_ORDERBOOKS_DEFAULT}")
    val saveOrderBooks: Boolean
) {

    @Bean
    fun connectorApiUrls(): List<String> {
        return exchangesConnectorApiUrls.split(",").map { url -> url.trim() }
    }

    @Bean
    fun orderBookSnapshotPeriod(): Long {
        return orderBookSnapshotPeriod convert MINUTES_TO_MILLIS
    }

    @Bean
    fun saveOrderBooks(): Boolean {
        return saveOrderBooks
    }

}