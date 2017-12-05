package fund.cyber.markets.rest.configuration

import fund.cyber.markets.helpers.env

const val CORS_ALLOWED_ORIGINS = "CORS_ALLOWED_ORIGINS"
const val CORS_ALLOWED_ORIGINS_DEFAULT = "search.cyber.fund"

object RestApiConfiguration {
    val allowedCORS: String = env(CORS_ALLOWED_ORIGINS, CORS_ALLOWED_ORIGINS_DEFAULT)
}