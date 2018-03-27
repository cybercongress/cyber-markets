package fund.cyber.markets.configuration

const val KAFKA_BROKERS = "KAFKA_BROKERS"
const val KAFKA_BROKERS_DEFAULT = "localhost:9092"

const val CASSANDRA_HOSTS = "CASSANDRA_HOSTS"
const val CASSANDRA_HOSTS_DEFAULT = "localhost"

const val CASSANDRA_PORT = "CASSANDRA_PORT"
const val CASSANDRA_PORT_DEFAULT = 9042

inline fun <reified T : Any> env(name: String, default: T): T =
        when (T::class) {
            String::class -> (System.getenv(name) ?: default) as T
            Int::class, Int::class.javaPrimitiveType -> (System.getenv(name)?.toIntOrNull() ?: default) as T
            Boolean::class, Boolean::class.javaPrimitiveType -> (System.getenv(name).toBoolean()) as T
            else -> default
        }