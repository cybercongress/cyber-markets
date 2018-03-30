package fund.cyber.markets.connector

import fund.cyber.markets.model.Order
import fund.cyber.markets.model.Trade
import info.bitrich.xchangestream.core.StreamingExchangeFactory
import info.bitrich.xchangestream.gdax.GDAXStreamingExchange
import org.knowm.xchange.dto.marketdata.OrderBook
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class GdaxConnector : BaseXchangeConnector() {
    override val exchange = StreamingExchangeFactory.INSTANCE.createExchange(GDAXStreamingExchange::class.java.name)!!

    @Autowired
    override lateinit var tradeKafkaTemplate: KafkaTemplate<String, Trade>

    @Autowired
    override lateinit var orderKafkaTemplate: KafkaTemplate<String, Order>

    @Autowired
    override lateinit var orderBookKafkaTemplate: KafkaTemplate<String, OrderBook>
}