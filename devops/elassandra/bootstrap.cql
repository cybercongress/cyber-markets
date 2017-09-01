CREATE KEYSPACE blockchains
  WITH REPLICATION = {
   'class' : 'NetworkTopologyStrategy',
   'DC1' : 1
  } ;

CREATE TABLE blockchains.ethereum(
     number text PRIMARY KEY,
     hash text,
     timestamp timestamp,
     rawblock text
  );