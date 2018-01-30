package fund.cyber.markets.tickers.processor

import fund.cyber.markets.dto.TokensPair
import fund.cyber.markets.helpers.add
import fund.cyber.markets.model.Ticker
import fund.cyber.markets.model.Trade
import fund.cyber.markets.tickers.AppContext
import fund.cyber.markets.tickers.configuration.TickersConfiguration
import fund.cyber.markets.tickers.service.TickerService
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.slf4j.LoggerFactory
import java.util.*

class HopTickerProcessor(
        private val configuration: TickersConfiguration = AppContext.configuration,
        private val windowHop: Long = configuration.windowHop,
        private val tickerService: TickerService = AppContext.tickerService,
        val hopTickers: MutableMap<TokensPair, MutableMap<String, Ticker>> = mutableMapOf()
): HopTickerProcessorInterface {

    private val log = LoggerFactory.getLogger(HopTickerProcessor::class.java)!!

    private var tradeRecords: ConsumerRecords<String, Trade>? = null

    override fun poll() {
        tradeRecords = tickerService.poll(windowHop / 2)
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