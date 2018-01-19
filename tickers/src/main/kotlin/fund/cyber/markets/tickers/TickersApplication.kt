package fund.cyber.markets.tickers

import fund.cyber.markets.cassandra.CassandraService
import fund.cyber.markets.kafka.JsonDeserializer
import fund.cyber.markets.kafka.JsonSerializer
import fund.cyber.markets.model.Ticker
import fund.cyber.markets.model.TickerKey
import fund.cyber.markets.model.Trade
import fund.cyber.markets.tickers.configuration.TickersConfiguration
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer


fun main(args: Array<String>) {

    val configuration = TickersConfiguration()
    configuration.createTickerTopic()

    val cassandraService = CassandraService(configuration.cassandraProperties)
    val tickerRepository = cassandraService.tickerRepository

    val consumer = KafkaConsumer<String, Trade>(
            configuration.consumerProperties,
            JsonDeserializer(String::class.java),
            JsonDeserializer(Trade::class.java)
    )

    val consumerBackup = KafkaConsumer<TickerKey, Ticker>(
            configuration.consumerTickersBackupsProperties,
            JsonDeserializer(TickerKey::class.java),
            JsonDeserializer(Ticker::class.java)
    )

    val producer = KafkaProducer<TickerKey, Ticker> (
            configuration.producerProperties,
            JsonSerializer<TickerKey>(),
            JsonSerializer<Ticker>()
    ).apply { initTransactions() }

    Runtime.getRuntime().addShutdownHook(object : Thread() {
        override fun run() {
            cassandraService.shutdown()
        }
    })

    TickersProcessor(
            configuration,
            consumer,
            consumerBackup,
            producer,
            tickerRepository
    ).process()

}