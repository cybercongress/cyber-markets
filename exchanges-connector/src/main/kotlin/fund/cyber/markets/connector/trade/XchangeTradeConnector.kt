package fund.cyber.markets.connector.trade

import fund.cyber.markets.common.MILLIS_TO_MINUTES
import fund.cyber.markets.common.convert
import fund.cyber.markets.common.model.TokensPair
import fund.cyber.markets.common.model.Trade
import fund.cyber.markets.common.model.TradeType
import fund.cyber.markets.connector.AbstractXchangeConnector
import fund.cyber.markets.connector.configuration.EXCHANGE_TAG
import fund.cyber.markets.connector.configuration.NINE_HUNDRED_NINGTHY_FIVE_PERCENT
import fund.cyber.markets.connector.configuration.NINGTHY_FIVE_PERCENT
import fund.cyber.markets.connector.configuration.TOKENS_PAIR_TAG
import fund.cyber.markets.connector.configuration.TOKEN_TAG
import fund.cyber.markets.connector.configuration.TRADE_COUNT_BY_TOKEN_METRIC
import fund.cyber.markets.connector.configuration.TRADE_COUNT_METRIC
import fund.cyber.markets.connector.configuration.TRADE_LATENCY_METRIC
import info.bitrich.xchangestream.core.ProductSubscription
import info.bitrich.xchangestream.core.StreamingExchangeFactory
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tags
import io.micrometer.core.instrument.Timer
import org.springframework.kafka.core.KafkaTemplate
import java.util.concurrent.TimeUnit

class XchangeTradeConnector: AbstractXchangeConnector {
    private constructor()

    constructor(streamingExchangeClassName: String, kafkaTemplate: KafkaTemplate<String, Any>, meterRegistry: MeterRegistry) : this() {
        exchange = StreamingExchangeFactory.INSTANCE.createExchange(streamingExchangeClassName)
        this.kafkaTemplate = kafkaTemplate
        this.monitoring = meterRegistry
    }

    override fun buildSubscription(): ProductSubscription {
        val subscription = ProductSubscription.create()
        exchangeTokensPairs.forEach { pair ->
            subscription.addTrades(pair)
        }
        return subscription.build()
    }

    override fun subscribe() {
        log.info("Subscribing for trades from $exchangeName exchange")

        val exchangeTag = Tags.of(EXCHANGE_TAG, exchangeName)

        exchangeTokensPairs.forEach { pair ->
            val exchangePairTag = exchangeTag.and(Tags.of(TOKENS_PAIR_TAG, pair.base.currencyCode + "_" + pair.counter.currencyCode))
            val baseTokenTag = exchangeTag.and(Tags.of(TOKEN_TAG, pair.base.currencyCode))
            val quoteTokenTag = exchangeTag.and(Tags.of(TOKEN_TAG, pair.counter.currencyCode))

            val baseTokenMonitor = monitoring.counter(TRADE_COUNT_BY_TOKEN_METRIC, baseTokenTag)
            val quoteTokenMonitor = monitoring.counter(TRADE_COUNT_BY_TOKEN_METRIC, quoteTokenTag)

            val tradePerSecondMonitor = monitoring.counter(TRADE_COUNT_METRIC, exchangePairTag)
            val tradeLatencyMonitor = Timer.builder(TRADE_LATENCY_METRIC)
                    .tags(exchangeTag)
                    .publishPercentiles(NINGTHY_FIVE_PERCENT, NINE_HUNDRED_NINGTHY_FIVE_PERCENT)
                    .register(monitoring)

            val tradeSubscription = exchange.streamingMarketDataService
                    .getTrades(pair)
                    .subscribe({ exchangeTrade ->
                        log.debug("$exchangeName trade: {}", exchangeTrade)

                        tradeLatencyMonitor.record(System.currentTimeMillis() - exchangeTrade.timestamp.time, TimeUnit.MILLISECONDS)
                        tradePerSecondMonitor.increment()
                        baseTokenMonitor.increment()
                        quoteTokenMonitor.increment()

                        val trade = convertTrade(exchangeName, exchangeTrade)
                        kafkaTemplate.send(tradesTopicName, trade)
                    }) { throwable ->
                        log.error("Error in subscribing trades for $exchangeName", throwable)
                    }
            subscriptions.add(tradeSubscription)
        }
    }

    private fun convertTrade(exchangeName: String, exchangeTrade: org.knowm.xchange.dto.marketdata.Trade): Trade {
        return Trade(
                exchangeName.toUpperCase(),
                TokensPair(exchangeTrade.currencyPair.base.currencyCode, exchangeTrade.currencyPair.counter.currencyCode),
                TradeType.valueOf(exchangeTrade.type.name),
                exchangeTrade.timestamp.time,
                exchangeTrade.timestamp.time convert MILLIS_TO_MINUTES,
                exchangeTrade.id,
                exchangeTrade.originalAmount,
                exchangeTrade.originalAmount.multiply(exchangeTrade.price),
                exchangeTrade.price
        )
    }
}