package fund.cyber.markets.ticker.service

import fund.cyber.markets.cassandra.model.CqlTokenTicker
import fund.cyber.markets.common.model.Trade
import org.apache.kafka.clients.consumer.ConsumerRecords

interface TickerKafkaService {
    fun skipOldTrades()
    fun pollTrades(timeout: Long): ConsumerRecords<String, Trade>
    fun send(tickers: MutableList<CqlTokenTicker>)
    fun backupTickers(tickers: MutableList<CqlTokenTicker>)
    fun pollBackupedTickers(timeout: Long): List<CqlTokenTicker>
}