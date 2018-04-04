package fund.cyber.markets.connector

import fund.cyber.markets.model.Trade
import info.bitrich.xchangestream.core.StreamingExchangeFactory
import info.bitrich.xchangestream.gdax.GDAXStreamingExchange
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class GdaxConnector : BaseXchangeConnector() {
    override val exchange = StreamingExchangeFactory.INSTANCE.createExchange(GDAXStreamingExchange::class.java.name)!!

    @Autowired
    override lateinit var tradeKafkaTemplate: KafkaTemplate<String, Trade>
}