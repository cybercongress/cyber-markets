package fund.cyber.markets.api.rest.service

import fund.cyber.markets.cassandra.common.toOrderBook
import fund.cyber.markets.cassandra.model.CqlTokensPair
import fund.cyber.markets.cassandra.model.CqlTrade
import fund.cyber.markets.cassandra.repository.OrderBookRepository
import fund.cyber.markets.cassandra.repository.TradeRepository
import fund.cyber.markets.common.MILLIS_TO_HOURS
import fund.cyber.markets.common.convert
import fund.cyber.markets.common.model.OrderBook
import fund.cyber.markets.common.model.TokensPair
import fund.cyber.markets.common.rest.service.ConnectorService
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

@Service
class RawDataService(
    private val connectorService: ConnectorService,
    private val orderBookRepository: OrderBookRepository,
    private val tradeRepository: TradeRepository
) {

    fun getOrderBook(exchange: String, pair: TokensPair, timestamp: Long?): Mono<OrderBook> {
        return if (timestamp != null) {
            orderBookRepository
                .findLastByTimestamp(exchange, CqlTokensPair(pair), timestamp convert MILLIS_TO_HOURS, Date(timestamp))
                .map { cqlOrderBook -> cqlOrderBook.toOrderBook() }
        } else {
            connectorService.getOrderBook(exchange, pair)
        }
    }

    fun getTrades(exchange: String, pair: TokensPair, epochMin: Long): Flux<CqlTrade> {
       return tradeRepository.find(exchange, CqlTokensPair(pair), epochMin)
    }

}