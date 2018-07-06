package fund.cyber.markets.api.rest.configuration

import fund.cyber.markets.common.CORS_ALLOWED_ORIGINS
import fund.cyber.markets.common.CORS_ALLOWED_ORIGINS_DEFAULT
import fund.cyber.markets.common.EXCHANGES_CONNECTOR_API_URLS
import fund.cyber.markets.common.EXCHANGES_CONNECTOR_API_URLS_DEFAULT
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsWebFilter
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource
import org.springframework.web.util.pattern.PathPatternParser


@Configuration
class RestApiConfiguration(
    @Value("\${$EXCHANGES_CONNECTOR_API_URLS:$EXCHANGES_CONNECTOR_API_URLS_DEFAULT}")
    private val exchangesConnectorApiUrls: String,

    @Value("\${$CORS_ALLOWED_ORIGINS:$CORS_ALLOWED_ORIGINS_DEFAULT}")
    private val allowedOrigin: String
) {

    @Bean
    fun connectorApiUrls(): List<String> {
        return exchangesConnectorApiUrls.split(",").map { url -> url.trim() }
    }

    @Bean
    fun corsFilter(): CorsWebFilter {

        val config = CorsConfiguration()
        config.addAllowedOrigin(allowedOrigin)
        config.addAllowedHeader("*")
        config.addAllowedMethod("*")

        val source = UrlBasedCorsConfigurationSource(PathPatternParser())
        source.registerCorsConfiguration("/**", config)

        return CorsWebFilter(source)
    }
}