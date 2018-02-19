package fund.cyber.markets.ticker.processor

import fund.cyber.markets.model.TokenVolume

interface VolumeProcessorInterface {

    fun update(hopTickerProcessor: HopTickerProcessor, tickerProcessor: TickerProcessor, currentMillis: Long)

    fun get(token: String, exchange: String, windowDuration: Long, currentMillis: Long): TokenVolume

    fun updateTimestamps(currentMillisHop: Long)

    fun saveAndProduceToKafka(currentMillisHop: Long)

}