package fund.cyber.markets.api.rest

import fund.cyber.markets.api.rest.handler.ExchangesInfoHandler
import fund.cyber.markets.api.rest.handler.PriceHandler
import fund.cyber.markets.api.rest.handler.RawDataHandler
import fund.cyber.markets.api.rest.handler.TickerHandler
import fund.cyber.markets.api.rest.handler.TokenHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.router

@Configuration
class ApiRouter(
    private val exchangesInfoHandler: ExchangesInfoHandler,
    private val rawDataHandler: RawDataHandler,
    private val tickerHandler: TickerHandler,
    private val priceHandler: PriceHandler,
    private val tokenHandler: TokenHandler
) {

    @Bean
    fun routes() = router {

        GET("/token/{tokenSymbol}", tokenHandler::getTokenInfo)

        GET("/exchanges", exchangesInfoHandler::getConnectedExchanges)

        GET("/exchanges/tokens", exchangesInfoHandler::getTokens)

        GET("/exchanges/tokens/count", exchangesInfoHandler::getTokensCount)

        GET("/exchange/{exchangeName}/tokens", exchangesInfoHandler::getTokensByExchange)

        GET("/exchange/{exchangeName}/tokens/count", exchangesInfoHandler::getTokensCountByExchange)

        GET("/exchange/{exchangeName}/pairs", exchangesInfoHandler::getPairs)

        GET("/orderbook", rawDataHandler::getOrderBook)

        GET("/trade", rawDataHandler::getTrades)

        GET("/ticker", tickerHandler::getTickers)

        GET("/price", priceHandler::getPrices)

        GET("/pricemulti", priceHandler::getMultiPrices)

    }

}