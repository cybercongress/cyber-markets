package fund.cyber.markets.tickers

import fund.cyber.markets.dto.TokensPair
import fund.cyber.markets.kafka.JsonSerde
import fund.cyber.markets.kafka.WindowedSerde
import fund.cyber.markets.model.Trade
import fund.cyber.markets.tickers.configuration.KafkaConfiguration
import fund.cyber.markets.tickers.configuration.tickersTopicName
import fund.cyber.markets.tickers.model.Ticker
import fund.cyber.markets.tickers.model.TickerKey
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.utils.Bytes
import org.apache.kafka.streams.Consumed
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.kstream.Materialized
import org.apache.kafka.streams.kstream.Produced
import org.apache.kafka.streams.kstream.Serialized
import org.apache.kafka.streams.kstream.TimeWindows
import org.apache.kafka.streams.kstream.Windowed
import org.apache.kafka.streams.state.WindowStore
import java.sql.Timestamp


fun main(args: Array<String>) {

    val configuration = KafkaConfiguration()

    val builder = StreamsBuilder()
    val tradeStream = builder.stream<String, Trade>(configuration.topicNamePattern, Consumed.with(Serdes.String(), JsonSerde(Trade::class.java)))

    val groupedByTokensPairTable = createTableGroupedByTokensPair(tradeStream, configuration.windowHop)
    val groupedByTokensPairAndExchangeTable = createTableGroupedByTokensPairAndExchange(tradeStream, configuration.windowHop)

    for (windowDuration in configuration.getWindowDurations()) {
        addToTickersTopic(
                groupedByTokensPairTable,
                groupedByTokensPairAndExchangeTable,
                windowDuration,
                tickersTopicName
        )
    }

    val streams = KafkaStreams(builder.build(), configuration.tickerStreamProperties())

    streams.cleanUp()
    streams.start()
}

fun addToTickersTopic(groupedByTokensPair: KStream<Windowed<TokensPair>, Ticker>,
                      groupedByTokensPairAndExchange: KStream<Windowed<String>, Ticker>,
                      windowDuration: Long,
                      topicName: String) {

    groupedByTokensPair
        .groupByKey(
            Serialized.with(WindowedSerde(JsonSerde(TokensPair::class.java)), JsonSerde(Ticker::class.java))
        )
        .windowedBy(TimeWindows.of(windowDuration))
        .aggregate(
            { Ticker(windowDuration) },
            { _, newValue, aggregate -> aggregate.add(newValue) },
            Materialized.`as`<Windowed<TokensPair>, Ticker, WindowStore<Bytes, ByteArray>>(
                "tickers-grouped-by-pairs-" + windowDuration + "ms")
                .withValueSerde( JsonSerde(Ticker::class.java)
            )
        )
        .mapValues { ticker ->
            ticker.calcPrice()
        }
        .mapValues({ ticker ->
            ticker.setExchangeString("ALL")
        })
        .toStream({ key, ticker ->
            TickerKey(ticker.tokensPair!!, windowDuration, Timestamp(key.window().start()))
        })
        .to(topicName, Produced.with(JsonSerde(TickerKey::class.java), JsonSerde(Ticker::class.java)))

    groupedByTokensPairAndExchange
        .groupByKey(
            Serialized.with(WindowedSerde(Serdes.String()), JsonSerde(Ticker::class.java))
        )
        .windowedBy(TimeWindows.of(windowDuration))
        .aggregate(
            { Ticker(windowDuration) },
            { _, newValue, aggregate -> aggregate.add(newValue) },
            Materialized.`as`<Windowed<String>, Ticker, WindowStore<Bytes, ByteArray>>(
                "tickers-grouped-by-pairs-and-exchange" + windowDuration + "ms")
                .withValueSerde(JsonSerde(Ticker::class.java)
            )
        )
        .mapValues { ticker ->
            ticker.calcPrice()
        }
        .toStream({ key, ticker ->
            TickerKey(ticker.tokensPair!!, windowDuration, Timestamp(key.window().start()))
        })
        .to(topicName, Produced.with(JsonSerde(TickerKey::class.java), JsonSerde(Ticker::class.java)))
}

fun createTableGroupedByTokensPair(tradeStream: KStream<String, Trade>, windowHop: Long): KStream<Windowed<TokensPair>, Ticker> {
    return tradeStream
            .groupBy(
                { _, trade -> trade.pair },
                Serialized.with(JsonSerde(TokensPair::class.java), JsonSerde(Trade::class.java))
            )
            .windowedBy(TimeWindows.of(windowHop).advanceBy(windowHop))
            .aggregate(
                { Ticker(windowHop) },
                { _, newValue, aggregate -> aggregate.add(newValue) },
                Materialized.`as`<TokensPair, Ticker, WindowStore<Bytes, ByteArray>>(
                    "tickers-grouped-by-pairs-" + windowHop + "ms")
                    .withValueSerde(JsonSerde(Ticker::class.java)
                )
            )
            .toStream()
}

fun createTableGroupedByTokensPairAndExchange(tradeStream: KStream<String, Trade>, windowHop: Long): KStream<Windowed<String>, Ticker> {
    return tradeStream
            .groupBy(
                { _, trade -> trade.exchange + "_" + trade.pair.base + "_" + trade.pair.quote },
                Serialized.with(Serdes.String(), JsonSerde(Trade::class.java))
            )
            .windowedBy(TimeWindows.of(windowHop).advanceBy(windowHop))
            .aggregate(
                { Ticker(windowHop) },
                { _, newValue, aggregate -> aggregate.add(newValue) },
                Materialized.`as`<String, Ticker, WindowStore<Bytes, ByteArray>>(
                    "tickers-grouped-by-pairs-and-exchange" + windowHop + "ms")
                    .withValueSerde(JsonSerde(Ticker::class.java)
                )
            )
            .toStream()
}