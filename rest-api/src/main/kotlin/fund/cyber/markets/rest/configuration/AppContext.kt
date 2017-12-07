package fund.cyber.markets.rest.configuration

import com.datastax.driver.core.Cluster
import com.fasterxml.jackson.databind.ObjectMapper
import fund.cyber.markets.dao.service.TickerDaoService

object AppContext {

    val jsonSerializer = ObjectMapper()
    val jsonDeserializer = ObjectMapper()

    val cassandra = Cluster.builder()
            .addContactPoints(*RestApiConfiguration.cassandraServers.toTypedArray())
            .withPort(RestApiConfiguration.cassandraPort)
            .withMaxSchemaAgreementWaitSeconds(30)
            .build().init()!!

    val tickerDaoService by lazy { TickerDaoService(cassandra) }
}