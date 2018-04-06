package fund.cyber.markets.connector.configuration

import fund.cyber.markets.connector.XchangeConnector
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
    fun xChangeConnector(streamingExchangeClassName: String): XchangeConnector {
        return XchangeConnector(streamingExchangeClassName, kafkaTemplate, meterRegistry)
    }

}