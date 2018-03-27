package fund.cyber.markets.ticker.processor

import fund.cyber.markets.model.TokensPair
import fund.cyber.markets.model.Trade
import fund.cyber.markets.model.TradeType
import fund.cyber.markets.ticker.common.CrossConversion
import fund.cyber.markets.ticker.configuration.TickersConfiguration
import fund.cyber.markets.ticker.service.TickerServiceImpl
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.common.TopicPartition
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Bean
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import org.springframework.test.context.junit4.SpringRunner
import java.math.BigDecimal

@RunWith(SpringRunner::class)
@SpringBootTest
@SpringJUnitConfig
class HopTickerProcessorTest {

    @TestConfiguration
    class HopTickerProcessorTestConfiguration {

/*        @MockBean
        private lateinit var tickerService: TickerServiceImpl*/

        @Bean
        fun hopTickerProcessor(): HopTickerProcessorImpl {
            return HopTickerProcessorImpl()
        }

        @Bean
        fun configuration(): TickersConfiguration {
            return TickersConfiguration()
        }

        @Bean
        fun crossConversion(): CrossConversion {
            return CrossConversion()
        }

    }

    @Autowired
    private lateinit var hopTickerProcessor: HopTickerProcessorImpl

    @MockBean
    private lateinit var tickerService: TickerServiceImpl

    @Before
    fun before() {
        val topicName = "trades"
        val topicPartition = TopicPartition(topicName, 0)
        val tradeRecordsList = mutableListOf<ConsumerRecord<String, Trade>>()

        for (index in 1..10) {
            tradeRecordsList
                    .add(ConsumerRecord(topicName, 0, index.toLong(), "key",
                            Trade(
                                "tradeId",
                                    "testEx",
                                    "timestamp123",
                                    TradeType.BUY,
                                    TokensPair("XMR", "BTC"),
                                    BigDecimal(123),
                                    BigDecimal(321),
                                    BigDecimal(100),
                                    false,
                                    555555L
                            ))
                    )
        }


        val map = mapOf(topicPartition to tradeRecordsList)
        val tradeRecords = ConsumerRecords<String, Trade>(map)

        Mockito.`when`(tickerService.poll())
                .thenReturn(tradeRecords)
    }

    @Test
    fun test() {

        hopTickerProcessor.update()

        println(hopTickerProcessor.hopTickers.toString())

    }

}