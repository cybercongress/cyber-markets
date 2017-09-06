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


# search
curl -XGET 'http://localhost:9200/blockchains/ethereum/1'
curl -XGET 'http://localhost:9200/blockchains/_mapping/ethereum?pretty=true'

curl -XGET 'localhost:9200/blockchains/_search?pretty'-H 'Content-Type: application/json' -d '{
  "query": {
    "match": {
      "rawblock": "0x62e4bacc5efb0941bf8784e431746b95b14a700eb7b1ecc81679e4b496b3fe3c"
    }
  }
}'

docker exec -it localelassandra_elassandra-1_1 bash

nodetool cfhistograms
nodetool status