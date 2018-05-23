package fund.cyber.markets.connector.api

import fund.cyber.markets.connector.api.handler.ConnectorInfoHandler
import fund.cyber.markets.connector.api.handler.OrderBookHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.router

@Configuration
class ConnectorApiRouter(
    private val connectorInfoHandler: ConnectorInfoHandler,
    private val orderBookHandler: OrderBookHandler
) {

    @Bean
    fun routes() = router {

        GET("/liveness", connectorInfoHandler::isAlive)


        GET("/exchanges", connectorInfoHandler::getConnectedExchanges)


        GET("/exchange/{exchangeName}/pairs", connectorInfoHandler::getPairs)


        GET("/orderbook", orderBookHandler::getOrderBook)

    }

}