CREATE KEYSPACE blockchains
  WITH REPLICATION = {
   'class' : 'NetworkTopologyStrategy',
   'PRODUCTION' : 2
  } ;


CREATE TABLE blockchains.meta(
     table_name text PRIMARY KEY,
     granularity_type text,
     granularity_value text
);


CREATE TABLE blockchains.ethereum (
     chunk_id varint,
     number varint,
     rawblock text,
     PRIMARY KEY (chunk_id, number)
) WITH CLUSTERING ORDER BY (number ASC)
    AND compaction = {'class': 'LeveledCompactionStrategy'};

CREATE TABLE blockchains.bitcoin (
     chunk_id varint,
     number varint,
     rawblock text,
     PRIMARY KEY (chunk_id, number)
) WITH CLUSTERING ORDER BY (number ASC)
    AND compaction = {'class': 'LeveledCompactionStrategy'};

INSERT INTO blockchains.meta(table_name, granularity_type, granularity_value)
   VALUES ('ethereum', 'chunk', '500');

INSERT INTO blockchains.meta(table_name, granularity_type, granularity_value)
   VALUES ('bitcoin', 'chunk', '25');