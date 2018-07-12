package fund.cyber.markets.ticker.service

import fund.cyber.markets.cassandra.common.toTrade
import fund.cyber.markets.cassandra.model.CqlTradeLastTimestamp
import fund.cyber.markets.cassandra.model.CqlTradeTemporary
import fund.cyber.markets.cassandra.repository.TradeLastTimestampRepository
import fund.cyber.markets.cassandra.repository.TradeTemporaryRepository
import fund.cyber.markets.common.MILLIS_TO_MINUTES
import fund.cyber.markets.common.convert
import fund.cyber.markets.common.model.Trade
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

private const val START_TIMESTAMP_DEFAULT = 1230995705000

@Service
class TradeService(
    private val tradeRepository: TradeTemporaryRepository,
    private val lastTimestampRepository: TradeLastTimestampRepository
) {
    private val log = LoggerFactory.getLogger(TradeService::class.java)!!

    fun getTrades(timestampFrom: Long, timestampTo: Long): Flux<Trade> {

        val epochMinuteFrom = timestampFrom convert MILLIS_TO_MINUTES
        val epochMinuteTo = (timestampTo - 1) convert MILLIS_TO_MINUTES

        var cqlTrades = Flux.empty<CqlTradeTemporary>()

        for (epochMinute in epochMinuteFrom..epochMinuteTo) {
            cqlTrades = cqlTrades.mergeWith(tradeRepository.findByEpochMinute(epochMinute))
        }

        return cqlTrades
            .map { cqlTrade -> cqlTrade.toTrade() }
    }

    fun getLastProcessedTimestamp(): Mono<CqlTradeLastTimestamp> {
        return lastTimestampRepository
            .findTradeLastTimestamp()
            .defaultIfEmpty(CqlTradeLastTimestamp(value = START_TIMESTAMP_DEFAULT))
    }

    fun updateLastProcessedTimestamp(timestamp: Long): Mono<CqlTradeLastTimestamp> {
        return lastTimestampRepository.save(CqlTradeLastTimestamp(value = timestamp))
    }

}