package fund.cyber.markets.common

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

val jsonSerializer = ObjectMapper().registerKotlinModule()
        .registerModule(Jdk8Module())
        .registerModule(JavaTimeModule())
        .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)!!

val jsonDeserializer = ObjectMapper().registerKotlinModule()
        .registerModule(Jdk8Module())
        .registerModule(JavaTimeModule())
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)!!
