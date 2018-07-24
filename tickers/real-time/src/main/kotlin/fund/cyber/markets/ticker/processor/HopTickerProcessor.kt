package fund.cyber.markets.ticker.processor

import fund.cyber.markets.common.closestSmallerMultiplyFromTs
import fund.cyber.markets.common.model.BaseTokens
import fund.cyber.markets.common.model.TokenTicker
import fund.cyber.markets.ticker.common.CrossConversion
import fund.cyber.markets.ticker.common.updateBaseVolumesWithPrices
import fund.cyber.markets.ticker.common.updateVolumes
import fund.cyber.markets.ticker.service.TickerService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.*

/**
 * @param hopTickers - map of token symbol -> TokenTicker
 */
@Component
class HopTickerProcessor(
    private val tickerService: TickerService,
    private val crossConversion: CrossConversion,
    private val windowHop: Long
) {
    private val log = LoggerFactory.getLogger(HopTickerProcessor::class.java)!!

    var currentHopTo: Long = 0L

    fun getHopTickers(): MutableMap<String, TokenTicker>  {
        val hopTickers = mutableMapOf<String, TokenTicker>()

        currentHopTo = closestSmallerMultiplyFromTs(windowHop)
        val currentHopFrom = currentHopTo - windowHop

        val tradeRecords = tickerService.poll()
        val tradesCount = tradeRecords.count()
        log.debug("Trades count: {}", tradesCount)

        tradeRecords.forEach {
            log.debug("trade: ${Date(it.value().timestamp)} ; current: ${Date(currentHopFrom)} ")
        }

        //todo: magic filter
        val trades = tradeRecords
            .map { tradeRecord -> tradeRecord.value() }
            .filter { trade -> trade.timestamp + windowHop >= currentHopFrom }

        log.debug("Dropped trades count: {}", tradesCount - trades.size)

        crossConversion.updateMapOfPrices(trades)

        trades.forEach { trade ->
            updateVolumes(hopTickers, trade, currentHopFrom, windowHop)

            BaseTokens.values().forEach { baseToken ->
                updateBaseVolumesWithPrices(hopTickers, crossConversion, baseToken.name, trade, currentHopFrom, windowHop)
            }
        }

        return hopTickers
    }

}