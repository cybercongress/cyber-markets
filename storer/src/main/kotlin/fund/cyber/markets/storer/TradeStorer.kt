package fund.cyber.markets.storer

import fund.cyber.markets.cassandra.model.CqlTrade
import fund.cyber.markets.cassandra.model.CqlTradeTemporary
import fund.cyber.markets.cassandra.repository.TradeRepository
import fund.cyber.markets.cassandra.repository.TradeTemporaryRepository
import fund.cyber.markets.common.model.Trade
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux

@Component
class TradeStorer(
    val tradeRepository: TradeRepository,
    val tradeTemporaryRepository: TradeTemporaryRepository
) {

    @KafkaListener(id = "tradeListener", topicPattern = "TRADES-.*")
    fun tradeListener(trades: List<Trade>) {
        Flux.merge(
            tradeRepository.saveAll(trades.map { trade -> CqlTrade(trade) }),
            tradeTemporaryRepository.saveAll(trades.map { trade -> CqlTradeTemporary(trade) })
        )
            .collectList().block()
    }

}