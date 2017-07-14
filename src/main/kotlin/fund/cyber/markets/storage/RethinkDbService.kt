package fund.cyber.markets.storage

import com.rethinkdb.RethinkDB.r
import com.rethinkdb.model.MapObject
import fund.cyber.markets.model.Trade
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct


val tradesTable: String = "trades"


@Component
open class RethinkDbService(
        val connectionPool: RethinkDbConnectionPool,
        rethinkDbProperties: RethinkDbProperties
) {

    val dbName: String = rethinkDbProperties.dbName

    @PostConstruct
    private fun createDbAndTablesIfNotExist() {

        val connection = connectionPool.borrowObject()

        val dbExist: Boolean = r.dbList().contains(dbName).run(connection)
        if (!dbExist) r.dbCreate(dbName).run<Any>(connection)

        val tradesTableExists: Boolean = r.db(dbName).tableList().contains(tradesTable).run(connection)
        if (!tradesTableExists) r.db(dbName).tableCreate(tradesTable).run<Any>(connection)

        connectionPool.returnObject(connection)
    }

    open fun saveTrades(trades: List<Trade>) {

        val connection = connectionPool.borrowObject()

        val tradesObjects = trades.map { (id, exchange, timestamp, type, currencyPair, quantity, rate, total) ->
            val tradeObject = MapObject()
            tradeObject.with("tradeId", id)
            tradeObject.with("exchange", exchange)
            tradeObject.with("type", type.name)
            tradeObject.with("baseCurrency", currencyPair.baseCurrency)
            tradeObject.with("counterCurrency", currencyPair.counterCurrency)
            tradeObject.with("baseAmount", quantity.toPlainString())
            tradeObject.with("counterAmount", total.toPlainString())
            tradeObject.with("rate", rate.toPlainString())
            tradeObject.with("timestamp", timestamp)
        }.toList()

        r.db(dbName).table(tradesTable).insert(tradesObjects).run<Any>(connection)
        connectionPool.returnObject(connection)
    }
}