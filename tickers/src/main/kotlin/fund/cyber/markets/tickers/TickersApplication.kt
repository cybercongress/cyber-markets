package fund.cyber.markets.tickers

import fund.cyber.markets.dto.TokensPair
import fund.cyber.markets.kafka.JsonSerde
import fund.cyber.markets.model.Trade
import fund.cyber.markets.tickers.configuration.KafkaConfiguration
import fund.cyber.markets.tickers.model.WindowKey
import fund.cyber.markets.tickers.model.WindowStats
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.kstream.KStreamBuilder
import org.apache.kafka.streams.kstream.TimeWindows
import java.sql.Timestamp

val configuration: KafkaConfiguration = KafkaConfiguration()

fun main(args: Array<String>) {

    val builder = KStreamBuilder()
    val tradeStream = builder.stream<String, Trade>(Serdes.String(), JsonSerde(Trade::class.java), configuration.topicNamePattern)

    for (windowDuration in configuration.windowDurations()) {
        createWindowStatStream(tradeStream,
                windowDuration,
                configuration.windowHop,
                "WINDOW-"+windowDuration+"ms",
                "WINDOW-"+windowDuration+"ms")
    }

    val streams = KafkaStreams(builder, configuration.tickerStreamProperties())

    streams.cleanUp()
    streams.start()
}

fun createWindowStatStream(stream: KStream<String, Trade>,
                           windowDuration: Long,
                           windowHop: Long,
                           localStorageName: String,
                           topicName: String) {

    val groupedStream: KStream<WindowKey, WindowStats> =
            stream.groupBy( { key, trade -> trade.pair },
                    JsonSerde(TokensPair::class.java), JsonSerde(Trade::class.java)).aggregate(
                    { WindowStats() },
                    { aggKey, newValue, aggValue -> aggValue.add(newValue) },
                    TimeWindows.of(windowDuration).advanceBy(windowHop),
                    JsonSerde(WindowStats::class.java),
                    localStorageName)
                    .toStream({key, windowStats -> WindowKey(windowStats.tokensPair!!, Timestamp(key.window().start()))})
                    .mapValues({stats -> stats.calcPrice()})

    groupedStream.to(JsonSerde(WindowKey::class.java), JsonSerde(WindowStats::class.java), topicName)
}