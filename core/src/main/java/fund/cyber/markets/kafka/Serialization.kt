package fund.cyber.markets.kafka

import org.apache.kafka.common.serialization.Deserializer
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.common.serialization.Serializer


class JsonSerializer<T> : Serializer<T> {
    private val objectMapper = ObjectMapper()

    override fun serialize(topic: String, data: T): ByteArray {
        return objectMapper.writeValueAsString(data).toByteArray()
    }

    override fun configure(configs: MutableMap<String, *>?, isKey: Boolean) {}
    override fun close() {}
}

class JsonDeserializer<T>(private val type: Class<T>) : Deserializer<T> {
    private val objectMapper = ObjectMapper()

    override fun deserialize(topic: String, data: ByteArray): T {
        return objectMapper.readValue(data, type)
    }

    override fun configure(configs: MutableMap<String, *>, isKey: Boolean) {}
    override fun close() {}
}