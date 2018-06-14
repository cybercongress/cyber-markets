package fund.cyber.markets.common

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

val jsonSerializer = ObjectMapper()
    .enable(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS)
    .enable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN)
    .setSerializationInclusion(JsonInclude.Include.NON_NULL)
    .registerKotlinModule()
    .registerModule(Jdk8Module())
    .registerModule(JavaTimeModule())!!

val jsonDeserializer = ObjectMapper().registerKotlinModule()
    .registerModule(Jdk8Module())
    .registerModule(JavaTimeModule())
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)!!
