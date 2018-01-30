package fund.cyber.markets.tickers

import fund.cyber.markets.cassandra.CassandraService
import fund.cyber.markets.tickers.configuration.TickersConfiguration
import fund.cyber.markets.tickers.service.TickerService
import fund.cyber.markets.tickers.service.VolumeService

object AppContext {

    val configuration = TickersConfiguration()

    val cassandraService = CassandraService(configuration.cassandraProperties)

    val tickerService by lazy { TickerService(configuration, cassandraService.tickerRepository) }
    val volumeService by lazy { VolumeService(configuration, cassandraService.volumeRepository) }

}