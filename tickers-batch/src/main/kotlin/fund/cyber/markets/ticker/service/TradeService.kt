package fund.cyber.markets.ticker.service

import fund.cyber.markets.cassandra.common.toTrade
import fund.cyber.markets.cassandra.repository.TradeRepository
import fund.cyber.markets.common.MILLIS_TO_MINUTES
import fund.cyber.markets.common.convert
import fund.cyber.markets.common.model.Trade
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class TradeService(
    private val tradeRepository: TradeRepository
) {
    private val log = LoggerFactory.getLogger(TradeService::class.java)!!

    fun getTrades(timestampFrom: Long, timestampTo: Long): List<Trade> {

        val epochMinuteFrom = timestampFrom convert MILLIS_TO_MINUTES
        val epochMinuteTo = timestampTo convert MILLIS_TO_MINUTES

        return tradeRepository.findByInterval(epochMinuteFrom, epochMinuteTo)
            .collectList()
            .defaultIfEmpty(mutableListOf())
            .block()!!
            .map { cqlTrade -> cqlTrade.toTrade() }
    }

}