package fund.cyber.markets.api.configuration

import com.fasterxml.jackson.databind.ObjectMapper


object AppContext {
    val jsonSerializer = ObjectMapper()
    val jsonDeserializer = ObjectMapper()
}