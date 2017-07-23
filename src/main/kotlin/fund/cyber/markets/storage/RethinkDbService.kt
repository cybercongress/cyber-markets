package fund.cyber.markets.storage

import com.rethinkdb.RethinkDB.r
import com.rethinkdb.ast.ReqlAst
import com.rethinkdb.model.MapObject
import fund.cyber.markets.model.ExchangeMetadata
import fund.cyber.markets.model.HistoryGap
import fund.cyber.markets.model.Trade
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct


val tradesTable: String = "trades"
val exchangeMetaDataTable: String = "exchange_metadata"
val historyGapsTable: String = "history_gaps"

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

        val historyGapsTableExist: Boolean = r.db(dbName).tableList().contains(historyGapsTable).runGetResult()
        if (!historyGapsTableExist) r.db(dbName).tableCreate(historyGapsTable).run()
    }


    fun saveTrades(trades: List<Trade>) {
        val tradesObjects = trades.map(::tradeToTradeObj)
        r.db(dbName).table(tradesTable).insert(tradesObjects).run()
    }

    fun saveHistoryGap(gap: HistoryGap) {

        //open gap -> still have problems, or problems just started
        if (gap.endTime == null) {
            r.db(dbName).table(historyGapsTable).insert(gap).run()
            return
        }

        //closed gap, manage problems, working normally
        r.db(dbName).table(historyGapsTable)
                .filter { item ->
                    item.g("exchange").eq(gap.exchange).and(item.g("startTime").eq(gap.startTime))
                }
                .update { item -> r.hashMap("endTime", gap.endTime) }
                .run()
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

private fun tradeToTradeObj(trade: Trade): MapObject {
    return MapObject().apply {
        with("tradeId", trade.tradeId)
        with("exchange", trade.exchange)
        with("type", trade.type.name)
        with("baseToken", trade.tokensPair.base)
        with("quoteToken", trade.tokensPair.quote)
        with("baseAmount", trade.baseAmount.toPlainString())
        with("quoteAmount", trade.quoteAmount.toPlainString())
        with("spotPrice", trade.spotPrice.toPlainString())
        with("timestamp", trade.timestamp)
    }
}