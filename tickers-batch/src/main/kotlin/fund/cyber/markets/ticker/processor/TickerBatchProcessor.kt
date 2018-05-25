package fund.cyber.markets.ticker.processor

import fund.cyber.markets.cassandra.common.toTokenTicker
import fund.cyber.markets.cassandra.repository.TickerRepository
import fund.cyber.markets.common.closestSmallerMultiply
import fund.cyber.markets.common.model.TokenTicker
import fund.cyber.markets.common.model.Trade
import fund.cyber.markets.ticker.configuration.kafka.TRADES_TOPIC_NAME_PATTERN
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import java.util.*

@Component
class TickerBatchProcessor(
    private val tickerRepository: TickerRepository,
    private val windowDurations: MutableSet<Long>
) {

    @KafkaListener(id = "tradeListener", topicPattern = TRADES_TOPIC_NAME_PATTERN)
    fun getTradesBatch(trades: List<Trade>) {

        trades.forEach { trade ->

            val tickers = getTickersByTrade(trade)

            tickers.forEach { ticker ->

            }

        }

    }


    fun getTickersByTrade(trade: Trade): MutableList<TokenTicker> {
        val tickers = mutableListOf<TokenTicker>()

        windowDurations.forEach { windowDuration ->

            val timestamp = closestSmallerMultiply(trade.timestamp, windowDuration)

            val tickerBase = tickerRepository.find(trade.pair.base, Date(timestamp), windowDuration).block()?.toTokenTicker()
                ?: initTokenTicker(trade.pair.base, timestamp, windowDuration)

            val tickerQuote = tickerRepository.find(trade.pair.quote, Date(timestamp), windowDuration).block()?.toTokenTicker()
                ?: initTokenTicker(trade.pair.quote, timestamp, windowDuration)

            tickers.add(tickerBase)
            tickers.add(tickerQuote)
        }

        return tickers
    }

    private fun initTokenTicker(symbol: String, timestampFrom: Long, windowDuration: Long): TokenTicker {
        return TokenTicker(
            symbol = symbol,
            timestampFrom = timestampFrom,
            timestampTo = timestampFrom + windowDuration,
            interval = windowDuration
        )
    }

}