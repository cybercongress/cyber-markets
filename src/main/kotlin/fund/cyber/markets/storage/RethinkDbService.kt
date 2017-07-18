package fund.cyber.markets.storage

import com.rethinkdb.RethinkDB.r
import com.rethinkdb.ast.ReqlAst
import com.rethinkdb.model.MapObject
import fund.cyber.markets.model.ExchangeMetadata
import fund.cyber.markets.model.Trade
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct


val tradesTable: String = "trades"
val exchangeMetaDataTable: String = "exchange_metadata"


@Component
open class RethinkDbService(
        val connectionPool: RethinkDbConnectionPool,
        rethinkDbProperties: RethinkDbProperties
) {

    val dbName: String = rethinkDbProperties.dbName

    @PostConstruct
    private fun createDbAndTablesIfNotExist() {

        val dbExist: Boolean = r.dbList().contains(dbName).runGetResult()
        if (!dbExist) r.dbCreate(dbName).run()

        val tradesTableExist: Boolean = r.db(dbName).tableList().contains(tradesTable).runGetResult()
        if (!tradesTableExist) r.db(dbName).tableCreate(tradesTable).run()

        val metadataTableExist: Boolean = r.db(dbName).tableList().contains(exchangeMetaDataTable).runGetResult()
        if (!metadataTableExist) r.db(dbName).tableCreate(exchangeMetaDataTable).optArg("primary_key", "exchange").run()
    }


    fun saveTrades(trades: List<Trade>) {

        val tradesObjects = trades.map { (tradeId, exchange, timestamp, type, tokensPair,
                                                 baseAmount, spotPrice, quoteAmount) ->
            val tradeObject = MapObject()
            tradeObject.with("tradeId", tradeId)
            tradeObject.with("exchange", exchange)
            tradeObject.with("type", type.name)
            tradeObject.with("baseToken", tokensPair.base)
            tradeObject.with("quoteToken", tokensPair.quote)
            tradeObject.with("baseAmount", baseAmount.toPlainString())
            tradeObject.with("quoteAmount", quoteAmount.toPlainString())
            tradeObject.with("spotPrice", spotPrice.toPlainString())
            tradeObject.with("timestamp", timestamp)
        }.toList()

        r.db(dbName).table(tradesTable).insert(tradesObjects).run()
    }

    fun <M : ExchangeMetadata> getExchangeMetadata(exchange: String, clazz: Class<M>): M? {
        return r.db(dbName).table(exchangeMetaDataTable).get(exchange).runGetResult(clazz)
    }

    private fun ReqlAst.run() {
        val connection = connectionPool.borrowObject()
        runNoReply(connection)
        connectionPool.returnObject(connection)
    }

    private fun <T> ReqlAst.runGetResult(): T {
        val connection = connectionPool.borrowObject()
        val result = run<T>(connection)
        connectionPool.returnObject(connection)
        return result
    }

    private fun <T> ReqlAst.runGetResult(clazz: Class<T>): T {
        val connection = connectionPool.borrowObject()
        val result = run<T, T>(connection, clazz)
        connectionPool.returnObject(connection)
        return result
    }
}
