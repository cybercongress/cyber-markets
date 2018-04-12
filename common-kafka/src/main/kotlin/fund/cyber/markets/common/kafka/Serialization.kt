package fund.cyber.markets.common.kafka

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serializer
import org.slf4j.LoggerFactory

class JsonSerializer<T> : Serializer<T> {
    private val objectMapper = ObjectMapper()
            .registerKotlinModule()
            .registerModule(Jdk8Module())
            .registerModule(JavaTimeModule())

    override fun serialize(topic: String, data: T): ByteArray {
        return objectMapper.writeValueAsString(data).toByteArray()
    }

    override fun configure(configs: MutableMap<String, *>?, isKey: Boolean) {}
    override fun close() {}
}

class JsonDeserializer<T>(private val type: Class<T>) : Deserializer<T> {
    private val objectMapper = ObjectMapper()
            .registerKotlinModule()
            .registerModule(Jdk8Module())
            .registerModule(JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    private val log = LoggerFactory.getLogger(JsonDeserializer::class.java)

    override fun deserialize(topic: String, data: ByteArray): T {
        log.debug("topic $topic data size : ${data.size}")
        return objectMapper.readValue(data, type)
    }

    override fun configure(configs: MutableMap<String, *>, isKey: Boolean) {}
    override fun close() {}
}