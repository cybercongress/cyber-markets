package fund.cyber.markets.cassandra.common

import org.springframework.data.cassandra.core.cql.keyspace.CreateKeyspaceSpecification
import org.springframework.data.cassandra.core.cql.keyspace.DataCenterReplication

fun defaultKeyspaceSpecification(keyspaces: String): CreateKeyspaceSpecification {
    return CreateKeyspaceSpecification.createKeyspace(keyspaces)
            .withNetworkReplication(
                    DataCenterReplication.of("WITHOUT_REPLICATION", 1)
            )
            .ifNotExists()
}
