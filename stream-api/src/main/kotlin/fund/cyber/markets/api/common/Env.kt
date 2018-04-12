package fund.cyber.markets.api.common

inline fun <reified T : Any> env(name: String, default: T): T =
        when (T::class) {
            String::class -> (System.getenv(name) ?: default) as T
            Int::class, Int::class.javaPrimitiveType -> (System.getenv(name)?.toIntOrNull() ?: default) as T
            Boolean::class, Boolean::class.javaPrimitiveType -> (System.getenv(name).toBoolean()) as T
            else -> default
        }