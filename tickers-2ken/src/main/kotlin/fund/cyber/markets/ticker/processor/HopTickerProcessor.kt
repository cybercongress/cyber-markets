package fund.cyber.markets.ticker.processor

import fund.cyber.markets.helpers.add
import fund.cyber.markets.model.Ticker
import fund.cyber.markets.model.TokensPair
import fund.cyber.markets.model.Trade
import fund.cyber.markets.ticker.TickerService
import fund.cyber.markets.ticker.configuration.TickersConfiguration
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.*

@Component
class HopTickerProcessor(
        val hopTickers: MutableMap<TokensPair, MutableMap<String, Ticker>> = mutableMapOf()
): HopTickerProcessorInterface {

    private val log = LoggerFactory.getLogger(HopTickerProcessor::class.java)!!

    @Autowired
    lateinit var configuration: TickersConfiguration

    private val windowHop: Long by lazy { configuration.windowHop }

    @Autowired
    lateinit var tickerService: TickerService

    private var tradeRecords: ConsumerRecords<String, Trade>? = null

    override fun poll() {
        tradeRecords = tickerService.poll()
        log.debug("Trades count: {}", tradeRecords?.count())
    }

    override fun update(currentMillisHop: Long) {
        hopTickers.clear()
        var droppedCount = 0
        for (record in tradeRecords!!) {
            val currentMillisHopFrom = currentMillisHop - windowHop
            if (record.timestamp() < currentMillisHopFrom - windowHop) {
                droppedCount++
                continue
            }

            val trade = record.value()
            val ticker = hopTickers
                    .getOrPut(trade.pair, { mutableMapOf() })
                    .getOrPut(trade.exchange, {
                        Ticker(trade.pair, windowHop, trade.exchange, Date(currentMillisHop), Date(currentMillisHopFrom))
                    })
            val tickerAllExchange = hopTickers
                    .getOrPut(trade.pair, { mutableMapOf() })
                    .getOrPut("ALL", {
                        Ticker(trade.pair, windowHop, "ALL", Date(currentMillisHop), Date(currentMillisHopFrom))
                    })
            ticker add trade
            tickerAllExchange add trade
        }

        log.debug("Dropped trades count: {}", droppedCount)
    }

    override fun get(pair: TokensPair, exchange: String, currentMillisHop: Long): Ticker {
        return hopTickers
                .getOrPut(pair, { mutableMapOf() })
                .getOrPut(exchange, {
                    Ticker(pair, windowHop, exchange, Date(currentMillisHop), Date(currentMillisHop - windowHop))
                })
    }

}