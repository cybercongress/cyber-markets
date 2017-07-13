package fund.cyber.markets.storage

import com.rethinkdb.RethinkDB.r
import com.rethinkdb.net.Connection
import org.apache.commons.pool2.PooledObject
import org.apache.commons.pool2.PooledObjectFactory
import org.apache.commons.pool2.impl.DefaultPooledObject
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component


@Component
@ConfigurationProperties("rethink")
open class RethinkDbProperties {
    var host: String = "127.0.0.1"
    var port: Int = 28015
    var authKey: String? = null
    var dbName: String = "cyber_markets"
}

@Component
open class RethinkDbConnectionsFactory(
        val properties: RethinkDbProperties
) : PooledObjectFactory<Connection> {

    override fun activateObject(p: PooledObject<Connection>?) {}
    override fun passivateObject(p: PooledObject<Connection>?) {}

    override fun validateObject(pooledObject: PooledObject<Connection>?): Boolean {
        return pooledObject?.getObject()?.isOpen ?: false
    }

    override fun destroyObject(pooledObject: PooledObject<Connection>?) {
        if (pooledObject?.getObject()?.isOpen ?: false) {
            pooledObject?.getObject()?.close()
        }
    }

    override fun makeObject(): PooledObject<Connection> {
        var connectionBuilder: Connection.Builder = r.connection().hostname(properties.host).port(properties.port)
        if (properties.authKey?.trim()?.isEmpty() ?: false) {
            connectionBuilder = connectionBuilder.authKey(properties.authKey)
        }
        return DefaultPooledObject(connectionBuilder.connect())
    }
}