package fund.cyber.markets.storage

import com.rethinkdb.net.Connection
import org.apache.commons.pool2.impl.GenericObjectPool
import org.apache.commons.pool2.impl.GenericObjectPoolConfig
import org.springframework.stereotype.Component


@Component
open class RethinkDbConnectionPoolConfig : GenericObjectPoolConfig() {
    init {
        maxTotal = 100
        maxIdle = 100
        minIdle = 5
        jmxEnabled = false
    }
}

@Component
open class RethinkDbConnectionPool(
        factory: RethinkDbConnectionsFactory,
        poolConfig: RethinkDbConnectionPoolConfig
) : GenericObjectPool<Connection>(factory, poolConfig)