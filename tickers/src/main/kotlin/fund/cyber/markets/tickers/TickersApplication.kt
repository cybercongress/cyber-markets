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
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.kstream.KStreamBuilder
import org.apache.kafka.streams.kstream.TimeWindows
import org.apache.kafka.streams.kstream.Windowed
import java.sql.Timestamp


fun main(args: Array<String>) {

    val configuration = KafkaConfiguration()

    val builder = KStreamBuilder()
    val tradeStream = builder.stream<String, Trade>(Serdes.String(), JsonSerde(Trade::class.java), configuration.topicNamePattern)

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

    val streams = KafkaStreams(builder, configuration.tickerStreamProperties())

    streams.cleanUp()
    streams.start()
}

fun addToTickersTopic(groupedByTokensPair: KStream<Windowed<TokensPair>, Ticker>,
                      groupedByTokensPairAndExchange: KStream<Windowed<String>, Ticker>,
                      windowDuration: Long,
                      topicName: String) {

    groupedByTokensPair
        .groupByKey(
            WindowedSerde(JsonSerde(TokensPair::class.java)),
            JsonSerde(Ticker::class.java)
        )
        .aggregate(
            { Ticker(windowDuration) },
            { _, newValue, aggregate -> aggregate.add(newValue) },
            TimeWindows.of(windowDuration),
            JsonSerde(Ticker::class.java),
            "tickers-grouped-by-pairs-" + windowDuration + "ms"
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
        .to(JsonSerde(TickerKey::class.java), JsonSerde(Ticker::class.java), topicName)

    groupedByTokensPairAndExchange
        .groupByKey(
            WindowedSerde(Serdes.String()),
            JsonSerde(Ticker::class.java)
        )
        .aggregate(
            { Ticker(windowDuration) },
            { _, newValue, aggregate -> aggregate.add(newValue) },
            TimeWindows.of(windowDuration),
            JsonSerde(Ticker::class.java),
            "tickers-grouped-by-pairs-and-exchange" + windowDuration + "ms"
        )
        .mapValues { ticker ->
            ticker.calcPrice()
        }
        .toStream({ key, ticker ->
            TickerKey(ticker.tokensPair!!, windowDuration, Timestamp(key.window().start()))
        })
        .to(JsonSerde(TickerKey::class.java), JsonSerde(Ticker::class.java), topicName)
}

fun createTableGroupedByTokensPair(tradeStream: KStream<String, Trade>, windowHop: Long): KStream<Windowed<TokensPair>, Ticker> {
    return tradeStream
            .groupBy(
                { _, trade -> trade.pair },
                JsonSerde(TokensPair::class.java),
                JsonSerde(Trade::class.java)
            )
            .aggregate(
                { Ticker(windowHop) },
                { _, newValue, aggregate -> aggregate.add(newValue) },
                TimeWindows.of(windowHop).advanceBy(windowHop),
                JsonSerde(Ticker::class.java)
            )
            .toStream()
}

fun createTableGroupedByTokensPairAndExchange(tradeStream: KStream<String, Trade>, windowHop: Long): KStream<Windowed<String>, Ticker> {
    return tradeStream
            .groupBy(
                { _, trade -> trade.exchange + "_" + trade.pair.base + "_" + trade.pair.quote },
                Serdes.String(),
                JsonSerde(Trade::class.java)
            )
            .aggregate(
                { Ticker(windowHop) },
                { _, newValue, aggregate -> aggregate.add(newValue) },
                TimeWindows.of(windowHop).advanceBy(windowHop),
                JsonSerde(Ticker::class.java)
            )
            .toStream()
}