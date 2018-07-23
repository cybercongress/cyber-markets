package fund.cyber.markets.cassandra.migration

import com.datastax.driver.core.SimpleStatement
import com.datastax.driver.core.Statement


interface Migration {
    val id: String
    val applicationId: String
}

interface CassandraMigration : Migration {
    fun getStatements(): List<Statement>
}

class EmptyMigration : Migration {
    override val id: String = ""
    override val applicationId: String = ""
}

class CqlFileBasedMigration(
    override val id: String,
    override val applicationId: String,
    private val fileContent: String
) : CassandraMigration {

    override fun getStatements(): List<Statement> {

        return fileContent
            .split(";").map(String::trim)
            .filter { statement -> statement.isNotEmpty() }
            .map { statement -> "$statement;" }
            .map { statement -> SimpleStatement(statement) }
    }
}


