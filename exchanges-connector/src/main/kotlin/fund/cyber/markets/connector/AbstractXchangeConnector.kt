package fund.cyber.markets.connector

import fund.cyber.markets.common.model.TokensPair
import fund.cyber.markets.connector.configuration.TRADES_TOPIC_PREFIX
import info.bitrich.xchangestream.core.ProductSubscription
import info.bitrich.xchangestream.core.StreamingExchange
import io.micrometer.core.instrument.MeterRegistry
import io.reactivex.disposables.Disposable
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate

abstract class AbstractXchangeConnector : Connector {
    val log = LoggerFactory.getLogger(javaClass)!!
    lateinit var monitoring: MeterRegistry

    lateinit var exchange: StreamingExchange
    val exchangeName by lazy { exchange.exchangeSpecification.exchangeName.toUpperCase() }
    val exchangeTokensPairs by lazy { exchange.exchangeSymbols }
    val subscriptions = mutableListOf<Disposable>()

    val tradesTopicName by lazy { TRADES_TOPIC_PREFIX + exchangeName }
    lateinit var kafkaTemplate: KafkaTemplate<String, Any>

    override fun connect() {
        exchange.connect(
            buildSubscription()
        ).blockingAwait()
        log.info("Connected to the $exchangeName exchange")
    }

    abstract fun buildSubscription(): ProductSubscription

    override fun disconnect() {
        subscriptions.forEach { subscription ->
            subscription.dispose()
        }
        exchange.disconnect().subscribe {
            log.info("Disconnected from the $exchangeName exchange")
        }
    }

    override fun isAlive(): Boolean {
        //todo: investigate how to check health of a exchange
        return exchange.isAlive
    }

    override fun updateTokensPairs() {
        log.warn("Update tokens pair for $exchangeName exchange not implemented yet")
    }

    override fun getTokensPairs(): Set<TokensPair> {
        val pairs = mutableSetOf<TokensPair>()

        exchangeTokensPairs.forEach { tokensPair ->
            pairs.add(TokensPair(tokensPair.base.currencyCode, tokensPair.counter.currencyCode))
        }

        return pairs
    }
}