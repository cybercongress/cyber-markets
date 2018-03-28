package fund.cyber.markets.connector

import fund.cyber.markets.model.Order
import fund.cyber.markets.model.Trade
import info.bitrich.xchangestream.core.StreamingExchangeFactory
import info.bitrich.xchangestream.poloniex2.PoloniexStreamingExchange
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class PoloniexConnector : BaseXchangeConnector() {
    override val exchange = StreamingExchangeFactory.INSTANCE.createExchange(PoloniexStreamingExchange::class.java.name)!!

    @Autowired
    override lateinit var tradeKafkaTemplate: KafkaTemplate<String, Trade>

    @Autowired
    override lateinit var orderKafkaTemplate: KafkaTemplate<String, Order>
}