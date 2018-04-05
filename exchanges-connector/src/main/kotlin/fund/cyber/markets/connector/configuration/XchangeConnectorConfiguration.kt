package fund.cyber.markets.connector.configuration

import fund.cyber.markets.connector.XchangeConnector
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import org.springframework.context.annotation.Scope
import org.springframework.kafka.core.KafkaTemplate

@Configuration
class XchangeConnectorConfiguration {

    @Autowired
    private lateinit var kafkaTemplate: KafkaTemplate<String, Any>

    @Bean
    @Scope("prototype")
    @Lazy(true)
    fun xChangeConnector(streamingExchangeClassName: String): XchangeConnector {
        return XchangeConnector(streamingExchangeClassName, kafkaTemplate)
    }

}