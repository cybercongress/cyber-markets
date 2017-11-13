package fund.cyber.markets.kafka

import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.common.serialization.Serializer

class JsonSerde<T>(clazz: Class<T>) : Serde<T> {

    private var serializer: Serializer<T> = JsonSerializer()
    private var deserializer: Deserializer<T> = JsonDeserializer(clazz)

    override fun serializer(): Serializer<T> {
        return serializer
    }

    override fun deserializer(): Deserializer<T> {
        return deserializer
    }

    override fun configure(configs: MutableMap<String, *>?, isKey: Boolean) {
        serializer.configure(configs, isKey)
        deserializer.configure(configs, isKey)
    }

    override fun close() {
        serializer.close()
        deserializer.close()
    }

}