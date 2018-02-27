package fund.cyber.markets.ticker.service

import fund.cyber.markets.model.TokenTicker
import fund.cyber.markets.model.Trade
import org.apache.kafka.clients.consumer.ConsumerRecords

interface TickerService {
    fun poll(): ConsumerRecords<String, Trade>
    fun persist(tickers: MutableMap<String, MutableMap<Long, TokenTicker>>, currentHopFromMillis: Long)
}