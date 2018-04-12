package fund.cyber.markets.api.rest.controller

import fund.cyber.markets.cassandra.repository.TickerRepository
import fund.cyber.markets.common.closestSmallerMultiply
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@RestController
@RequestMapping("/tickers")
class TickerController {

    @Autowired
    private lateinit var tickersRepository: TickerRepository

    @GetMapping("/{tokenSymbol}")
    fun ticker(
            @PathVariable tokenSymbol: String,
            @RequestParam interval: Long,
            @RequestParam timestamp: Long
            ): Flux<Any> {

        val ticker = tickersRepository.getTicker(tokenSymbol, interval, closestSmallerMultiply(timestamp, interval))

        return Flux.just(ticker ?: "No data")
    }

}