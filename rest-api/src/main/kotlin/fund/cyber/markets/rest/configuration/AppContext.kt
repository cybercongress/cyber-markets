package fund.cyber.markets.rest.configuration

import fund.cyber.markets.cassandra.CassandraService
import fund.cyber.markets.helpers.env

object AppContext {
    val cassandraService = CassandraService(RestApiConfiguration.cassandraProperties)
    val tickerRepository by lazy { cassandraService.tickerRepository }
    val volumeRepository by lazy { cassandraService.volumeRepository }
    val supplyRepository by lazy { cassandraService.supplyRepository }

    val CYBER_CHAINGEAR_API = env("CYBER_CHAINGEAR_API", "http://93.125.26.210:32600")
}