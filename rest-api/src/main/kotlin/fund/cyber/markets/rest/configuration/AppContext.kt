package fund.cyber.markets.rest.configuration

import fund.cyber.markets.cassandra.CassandraService

object AppContext {
    val cassandraService = CassandraService(RestApiConfiguration.cassandraProperties)
    val tickerRepository by lazy { cassandraService.tickerRepository }
}