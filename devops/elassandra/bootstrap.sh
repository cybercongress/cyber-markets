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