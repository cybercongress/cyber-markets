package fund.cyber.markets.kafka

import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.common.serialization.Serializer
import org.apache.kafka.streams.kstream.Windowed
import org.apache.kafka.streams.kstream.internals.WindowedDeserializer
import org.apache.kafka.streams.kstream.internals.WindowedSerializer


class WindowedSerde<T>(serde: Serde<T>) : Serde<Windowed<T>> {

    private val inner: Serde<Windowed<T>> = Serdes.serdeFrom<Windowed<T>>(
                WindowedSerializer<T>(serde.serializer()),
                WindowedDeserializer<T>(serde.deserializer())
            )

    override fun serializer(): Serializer<Windowed<T>> {
        return inner.serializer()
    }

    override fun deserializer(): Deserializer<Windowed<T>> {
        return inner.deserializer()
    }

    override fun configure(configs: Map<String, *>, isKey: Boolean) {
        inner.serializer().configure(configs, isKey)
        inner.deserializer().configure(configs, isKey)
    }

    override fun close() {
        inner.serializer().close()
        inner.deserializer().close()
    }

}