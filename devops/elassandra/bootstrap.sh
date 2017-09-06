#!/usr/bin/env bash

# mapping
curl -XPUT "http://localhost:9200/blockchains/" -d '{
   "settings" : { "keyspace" : "blockchains" } },
}'


curl -XPUT 'http://localhost:9200/blockchains/_mapping/ethereum' -d '{
        "ethereum" : {
            "properties" : {
              "rawblock" : { "type" : "string", "index" : "analyzed","cql_collection" : "singleton"}
            }
        }
}'

curl -XPUT 'http://localhost:9200/blockchains/_mapping/bitcoin' -d '{
        "bitcoin" : {
            "properties" : {
              "rawblock" : { "type" : "string", "index" : "analyzed","cql_collection" : "singleton"}
            }
        }
}'


# search
curl -XGET 'http://localhost:9200/blockchains/ethereum/1';
curl -XGET 'http://localhost:9200/blockchains/_mapping/ethereum?pretty=true'

curl -XGET 'localhost:9200/blockchains/_search?pretty' -H 'Content-Type: application/json' -d '{
  "query": {
    "match": {
      "rawblock": "ehtereum 00000000a5ae2c2c8b85706a041592fd81b9d3f4f56e8e760999003bd1eaed93"
    }
  }
}'

docker exec -it localelassandra_elassandra-1_1 bash

nodetool cfhistograms
nodetool status