package fund.cyber.markets.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serializer
import org.slf4j.LoggerFactory

class JsonSerializer<T> : Serializer<T> {
    private val objectMapper = ObjectMapper()

    override fun serialize(topic: String, data: T): ByteArray {
        return objectMapper.writeValueAsString(data).toByteArray()
    }

    override fun configure(configs: MutableMap<String, *>?, isKey: Boolean) {}
    override fun close() {}
}

class JsonDeserializer<T>(private val type: Class<T>) : Deserializer<T> {
    private val objectMapper = ObjectMapper().registerKotlinModule()
    private val LOGGER = LoggerFactory.getLogger(JsonDeserializer::class.java)!!

    override fun deserialize(topic: String, data: ByteArray?): T? {
        try {
            return if (data == null)
                null
             else
                objectMapper.readValue(data, type)
        } catch (e: Exception) {
            //kafka consumer just suppress exception without logging
            LOGGER.error("Exception during deserialization '$topic' topics entity", e)
            throw RuntimeException("Exception during deserialization '$topic' topics entity")
        }
    }

    override fun configure(configs: MutableMap<String, *>, isKey: Boolean) {}
    override fun close() {}
}
