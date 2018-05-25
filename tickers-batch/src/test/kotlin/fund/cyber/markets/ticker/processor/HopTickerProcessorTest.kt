package fund.cyber.markets.ticker.processor

/*
@RunWith(SpringRunner::class)
@SpringBootTest
@SpringJUnitConfig
class HopTickerProcessorTest {

    @TestConfiguration
    class HopTickerProcessorTestConfiguration {

*/
/*        @MockBean
        private lateinit var tickerService: TickerServiceImpl*//*


        @Bean
        fun hopTickerProcessor(): HopTickerProcessor {
            return HopTickerProcessor()
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
    private lateinit var hopTickerProcessor: HopTickerProcessor

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
                                "testExchange",
                                    TokensPair("XMR", "BTC"),
                                    TradeType.BID,
                                    Date(),
                                    "testTradeId",
                                    BigDecimal(100),
                                    BigDecimal(300),
                                    BigDecimal(3)
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

}*/
