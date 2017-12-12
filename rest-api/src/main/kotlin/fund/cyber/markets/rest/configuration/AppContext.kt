package fund.cyber.markets.rest.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import fund.cyber.markets.dao.DaoModule

object AppContext {

    val jsonSerializer = ObjectMapper()
    val jsonDeserializer = ObjectMapper()

    val daoModule = DaoModule(RestApiConfiguration.cassandraProperties)

    val tickerDaoService by lazy { daoModule.tickersDaoService }
}