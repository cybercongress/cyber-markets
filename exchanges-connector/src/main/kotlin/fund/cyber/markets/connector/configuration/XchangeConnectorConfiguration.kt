package fund.cyber.markets.connector.configuration

import fund.cyber.markets.connector.orderbook.XchangeOrderbookConnector
import fund.cyber.markets.connector.trade.XchangeTradeConnector
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import org.springframework.context.annotation.Scope
import org.springframework.kafka.core.KafkaTemplate

@Configuration
class XchangeConnectorConfiguration(
        private val kafkaTemplate: KafkaTemplate<String, Any>,
        private val meterRegistry: MeterRegistry
) {

    @Bean
    @Scope("prototype")
    @Lazy(true)
    fun xChangeTradeConnector(streamingExchangeClassName: String): XchangeTradeConnector {
        return XchangeTradeConnector(streamingExchangeClassName, kafkaTemplate, meterRegistry)
    }

    @Bean
    @Scope("prototype")
    @Lazy(true)
    fun xChangeOrderBookConnector(streamingExchangeClassName: String): XchangeOrderbookConnector {
        return XchangeOrderbookConnector(streamingExchangeClassName, kafkaTemplate, meterRegistry)
    }
}