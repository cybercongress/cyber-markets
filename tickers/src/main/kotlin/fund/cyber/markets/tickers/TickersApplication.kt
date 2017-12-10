package fund.cyber.markets.tickers

import fund.cyber.markets.dao.DaoModule
import fund.cyber.markets.kafka.JsonDeserializer
import fund.cyber.markets.kafka.JsonSerializer
import fund.cyber.markets.model.Trade
import fund.cyber.markets.tickers.configuration.TickersConfiguration
import fund.cyber.markets.model.Ticker
import fund.cyber.markets.model.TickerKey
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer


fun main(args: Array<String>) {

    val configuration = TickersConfiguration()
    configuration.createTickerTopic()

    val tickersDaoService = DaoModule(configuration.cassandraProperties).tickersDaoService!!

    val consumer = KafkaConsumer<String, Trade>(
            configuration.consumerProperties,
            JsonDeserializer(String::class.java),
            JsonDeserializer(Trade::class.java)
    )

    val producer = KafkaProducer<TickerKey, Ticker> (
            configuration.producerProperties,
            JsonSerializer<TickerKey>(),
            JsonSerializer<Ticker>()
    ).apply { initTransactions() }

    TickersProcessor(
            configuration,
            consumer,
            producer,
            tickersDaoService
    ).process()

}