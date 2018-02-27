package fund.cyber.markets.ticker.processor

import fund.cyber.markets.model.TokenTicker

interface TickerProcessor {
    fun process()
    fun update(hopTickers: MutableMap<String, TokenTicker>)
    fun saveAndProduceToKafka()
    fun updateTimestamps()
}