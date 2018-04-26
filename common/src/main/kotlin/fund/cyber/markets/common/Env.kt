package fund.cyber.markets.common

const val KAFKA_BROKERS = "KAFKA_BROKERS"
const val KAFKA_BROKERS_DEFAULT = "localhost:9092"

const val CASSANDRA_HOSTS = "CASSANDRA_HOSTS"
const val CASSANDRA_HOSTS_DEFAULT = "localhost"

const val CASSANDRA_PORT = "CASSANDRA_PORT"
const val CASSANDRA_PORT_DEFAULT = 9042

const val EXCHANGES = "EXCHANGES"
const val EXCHANGES_DEFAULT = "bitfinex,bitflyer,binance,bitstamp,gdax,gemini,hitbtc,okcoin,okex,poloniex,etherdelta"

const val PARITY_URL = "PARITY_URL"
const val PARITY_URL_DEFAULT = "http://127.0.0.1:8545"

const val WINDOW_DURATIONS_MIN = "WINDOW_DURATIONS_MIN"
const val WINDOW_DURATIONS_MIN_DEFAULT = "1,5,15,30,60,180,240,360,720,1440"

const val WINDOW_HOP_SEC = "WINDOW_HOP_SEC"
const val WINDOW_HOP_SEC_DEFAULT = 3

const val ALLOW_NOT_CLOSED_WINDOWS = "ALLOW_NOT_CLOSED_WINDOWS"
const val ALLOW_NOT_CLOSED_WINDOWS_DEFAULT = true