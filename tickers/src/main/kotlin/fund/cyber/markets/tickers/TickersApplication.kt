package fund.cyber.markets.tickers

import fund.cyber.markets.dao.DaoModule
import fund.cyber.markets.dto.TokensPair
import fund.cyber.markets.kafka.JsonSerde
import fund.cyber.markets.model.Ticker
import fund.cyber.markets.model.TickerKey
import fund.cyber.markets.model.Trade
import fund.cyber.markets.tickers.configuration.KafkaConfiguration
import fund.cyber.markets.tickers.configuration.cassandraConfiguration
import fund.cyber.markets.tickers.configuration.createTickerTopic
import fund.cyber.markets.tickers.configuration.tickersTopicName
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.utils.Bytes
import org.apache.kafka.streams.Consumed
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.kstream.Materialized
import org.apache.kafka.streams.kstream.Produced
import org.apache.kafka.streams.kstream.Serialized
import org.apache.kafka.streams.kstream.TimeWindows
import org.apache.kafka.streams.kstream.Window
import org.apache.kafka.streams.kstream.Windowed
import org.apache.kafka.streams.state.WindowStore
import java.sql.Timestamp

val daoModule = DaoModule(cassandraConfiguration())

fun main(args: Array<String>) {

    val configuration = KafkaConfiguration()
    createTickerTopic(configuration.kafkaServers)

    val builder = StreamsBuilder()
    val tradeStream = builder.stream<String, Trade>(configuration.topicNamePattern, Consumed.with(Serdes.String(), JsonSerde(Trade::class.java)))

    for (windowDuration in configuration.getWindowDurations()) {
        addToTickersTopic(
                tradeStream,
                windowDuration,
                configuration.windowHop,
                tickersTopicName
        )
    }

    val streams = KafkaStreams(builder.build(), configuration.tickerStreamProperties())

    streams.cleanUp()
    streams.start()
}

fun addToTickersTopic(tradeStream: KStream<String, Trade>,
                      windowDuration: Long,
                      windowHop: Long,
                      topicName: String) {

    val aggregatedByTokensPairStream = tradeStream
        .groupBy(
            { _, trade -> trade.pair },
            Serialized.with(JsonSerde(TokensPair::class.java), JsonSerde(Trade::class.java))
        )
        .windowedBy(TimeWindows.of(windowDuration).advanceBy(windowHop).until(windowDuration))
        .aggregate(
            { Ticker(windowDuration) },
            { _, newValue, aggregate -> aggregate.add(newValue) },
            Materialized.`as`<TokensPair, Ticker, WindowStore<Bytes, ByteArray>>(
                "tickers-grouped-by-pairs-" + windowDuration + "ms")
                .withValueSerde(JsonSerde(Ticker::class.java)
            )
        )
        .toStream()

    aggregatedByTokensPairStream
        .filter { tokensPair, _ -> isFinishedWindow(tokensPair.window(), windowHop) }
        .mapValues { ticker -> ticker.calcPrice() }
        .mapValues({ ticker ->
            ticker.setExchangeString("ALL")
        })
        .map { tokensPair, ticker -> KeyValue(
            tokensPair,
            ticker.setTimestamps(tokensPair.window().start(), tokensPair.window().end())
        )}
        .map { tokensPair, ticker -> KeyValue(
            tokensPair,
            saveSnapshots(tokensPair, ticker, windowDuration, windowHop)
        )}
        .map { tokensPair, ticker -> KeyValue(
            TickerKey(ticker.tokensPair!!, windowDuration, Timestamp(tokensPair.window().start())),
            ticker
        )}
        .to(topicName, Produced.with(JsonSerde(TickerKey::class.java), JsonSerde(Ticker::class.java)))

    val aggregatedByExchangeStream = tradeStream
        .groupBy(
            { _, trade -> trade.exchange + "_" + trade.pair.base + "_" + trade.pair.quote },
            Serialized.with(Serdes.String(), JsonSerde(Trade::class.java))
        )
        .windowedBy(TimeWindows.of(windowDuration).advanceBy(windowHop).until(windowDuration))
        .aggregate(
            { Ticker(windowDuration) },
            { _, newValue, aggregate -> aggregate.add(newValue) },
            Materialized.`as`<String, Ticker, WindowStore<Bytes, ByteArray>>(
                "tickers-grouped-by-pairs-and-exchange" + windowDuration + "ms")
                .withValueSerde(JsonSerde(Ticker::class.java)
            )
        )
        .toStream()

    aggregatedByExchangeStream
        .filter { tokensPair, _ -> isFinishedWindow(tokensPair.window(), windowHop) }
        .mapValues { ticker -> ticker.calcPrice() }
        .map { tokensPair, ticker -> KeyValue(
            tokensPair,
            ticker.setTimestamps(tokensPair.window().start(), tokensPair.window().end())
        )}
        .map { tokensPair, ticker -> KeyValue(
            tokensPair,
            saveSnapshots(tokensPair, ticker, windowDuration, windowHop)
        )}
        .map { tokensPair, ticker -> KeyValue(
            TickerKey(ticker.tokensPair!!, windowDuration, Timestamp(tokensPair.window().start())),
            ticker
        )}
        .to(topicName, Produced.with(JsonSerde(TickerKey::class.java), JsonSerde(Ticker::class.java)))
}

fun isFinishedWindow(window: Window, windowHop: Long): Boolean {
    return window.end() > System.currentTimeMillis()
            && Math.abs(window.end() - System.currentTimeMillis()) <= windowHop
}

fun saveSnapshots(windowedKey: Windowed<*>, ticker: Ticker, windowDuration: Long, windowHop: Long): Ticker {
    val window = windowedKey.window()

    if (isFinishedWindow(window, windowHop)
            && window.end() % windowDuration == 0L) {
        daoModule.tickersDaoService?.insert(ticker)
    }

    return ticker
}