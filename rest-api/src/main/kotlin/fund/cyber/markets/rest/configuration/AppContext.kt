package fund.cyber.markets.rest.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import fund.cyber.markets.cassandra.CassandraService

object AppContext {

    val jsonSerializer = ObjectMapper()

    val cassandraService = CassandraService(RestApiConfiguration.cassandraProperties)

    val tickerRepository by lazy { cassandraService.tickerRepository }
}