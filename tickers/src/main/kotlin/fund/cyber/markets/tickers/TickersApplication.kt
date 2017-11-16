package fund.cyber.markets.tickers

import fund.cyber.markets.dto.TokensPair
import fund.cyber.markets.kafka.JsonSerde
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
import java.sql.Timestamp

val configuration: KafkaConfiguration = KafkaConfiguration()

fun main(args: Array<String>) {

    val builder = KStreamBuilder()
    val tradeStream = builder.stream<String, Trade>(Serdes.String(), JsonSerde(Trade::class.java), configuration.topicNamePattern)

    for (windowDuration in configuration.windowDurations()) {
        createWindowStatStream(tradeStream,
                windowDuration,
                configuration.windowHop)
    }

    val streams = KafkaStreams(builder, configuration.tickerStreamProperties())

    streams.cleanUp()
    streams.start()
}

fun createWindowStatStream(stream: KStream<String, Trade>,
                           windowDuration: Long,
                           windowHop: Long) {

    val groupedByPairStream: KStream<TickerKey, Ticker> =
            stream
                    .groupBy(
                            { key, trade -> trade.pair },
                            JsonSerde(TokensPair::class.java),
                            JsonSerde(Trade::class.java)
                    )
                    .aggregate(
                            { Ticker() },
                            { aggKey, newValue, aggValue -> aggValue.add(newValue, windowDuration) },
                            TimeWindows.of(windowDuration).advanceBy(windowHop),
                            JsonSerde(Ticker::class.java),
                            "grouped-by-tokens-pair-" +windowDuration+ "ms-store"
                    )
                    .toStream({ key, ticker ->
                        TickerKey(ticker.tokensPair!!, windowDuration, Timestamp(key.window().start()))
                    })
                    .mapValues({ ticker ->
                        ticker.calcPrice()
                    })
                    .mapValues({ ticker ->
                        ticker.setExchangeString("ALL")
                    })

    val groupedByPairAndExchangeStream: KStream<TickerKey, Ticker> =
            stream
                    .groupBy(
                            { key, trade -> trade.exchange + trade.pair.toString() },
                            Serdes.String(),
                            JsonSerde(Trade::class.java)
                    )
                    .aggregate(
                            { Ticker() },
                            { aggKey, newValue, aggValue -> aggValue.add(newValue, windowDuration) },
                            TimeWindows.of(windowDuration).advanceBy(windowHop),
                            JsonSerde(Ticker::class.java),
                            "grouped-by-tokens-pair-and-exchange-"+windowDuration+"ms-store"
                    )
                    .toStream({ key, ticker ->
                        TickerKey(ticker.tokensPair!!, windowDuration, Timestamp(key.window().start()))
                    })
                    .mapValues({
                        ticker -> ticker.calcPrice()
                    })

    groupedByPairStream.to(JsonSerde(TickerKey::class.java), JsonSerde(Ticker::class.java), tickersTopicName)
    groupedByPairAndExchangeStream.to(JsonSerde(TickerKey::class.java), JsonSerde(Ticker::class.java), tickersTopicName)
}