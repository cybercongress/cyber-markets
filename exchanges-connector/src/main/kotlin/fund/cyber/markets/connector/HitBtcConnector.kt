package fund.cyber.markets.connector

import fund.cyber.markets.model.Trade
import info.bitrich.xchangestream.core.StreamingExchangeFactory
import info.bitrich.xchangestream.hitbtc.HitbtcStreamingExchange
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class HitBtcConnector : BaseXchangeConnector() {
    override val exchange = StreamingExchangeFactory.INSTANCE.createExchange(HitbtcStreamingExchange::class.java.name)!!

    @Autowired
    override lateinit var tradeKafkaTemplate: KafkaTemplate<String, Trade>
}