package fund.cyber.markets.storer

import fund.cyber.markets.cassandra.model.CqlTrade
import fund.cyber.markets.cassandra.repository.TradeRepository
import fund.cyber.markets.common.model.Trade
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class TradeStorer(
    val tradeRepository: TradeRepository
) {

    @KafkaListener(id = "tradeListener", topicPattern = "TRADES-.*")
    fun tradeListener(trades: List<Trade>) {
        tradeRepository.saveAll(trades.map { trade -> CqlTrade(trade) }).collectList().block()
    }

}