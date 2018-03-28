package fund.cyber.markets.connector

import fund.cyber.markets.model.Order
import fund.cyber.markets.model.Trade
import info.bitrich.xchangestream.bitfinex.BitfinexStreamingExchange
import info.bitrich.xchangestream.core.StreamingExchangeFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class BitfinexConnector : BaseXchangeConnector() {
    override val exchange = StreamingExchangeFactory.INSTANCE.createExchange(BitfinexStreamingExchange::class.java.name)!!

    @Autowired
    override lateinit var tradeKafkaTemplate: KafkaTemplate<String, Trade>

    @Autowired
    override lateinit var orderKafkaTemplate: KafkaTemplate<String, Order>
}