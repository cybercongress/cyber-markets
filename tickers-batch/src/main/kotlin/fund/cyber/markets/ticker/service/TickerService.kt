package fund.cyber.markets.ticker.service

import fund.cyber.markets.cassandra.model.CqlTokenTicker
import fund.cyber.markets.cassandra.repository.TickerRepository
import fund.cyber.markets.common.model.TokenTicker
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class TickerService(
    private val tickerRepository: TickerRepository
) {
    private val log = LoggerFactory.getLogger(TickerService::class.java)!!

    fun save(tickers : MutableCollection<TokenTicker>) {
        log.info("Saving tickers. Count: ${tickers.size}")

        tickerRepository.saveAll(tickers.map { CqlTokenTicker(it) }).collectList().block()
    }

}